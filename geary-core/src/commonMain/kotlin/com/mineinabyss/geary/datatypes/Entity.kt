package com.mineinabyss.geary.datatypes

import com.mineinabyss.geary.annotations.optin.DangerousComponentOperation
import com.mineinabyss.geary.components.EntityName
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.engine.Engine
import com.mineinabyss.geary.helpers.*
import com.mineinabyss.geary.modules.ArchetypeEngineModule
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.observers.events.OnAdd
import com.mineinabyss.geary.systems.accessors.AccessorOperations
import com.mineinabyss.geary.systems.accessors.RelationWithData
import kotlin.reflect.KClass

typealias GearyEntity = Entity

/**
 * A wrapper around [EntityId] that gets inlined to just a long (no performance degradation since no boxing occurs).
 *
 * Provides some useful functions, so we aren't forced to go through [Engine] every time we want to do some things.
 */
//TODO add require checks for entities across different worlds
class Entity(val id: EntityId, val world: Geary) {
    inline val idL get() = id.toLong()

    val geary get() = world.module
    private val entityProvider get() = world.module.entityProvider
    private val entityRemoveProvider get() = world.module.entityRemoveProvider
    private val queryManager get() = world.module.queryManager
    private val read get() = world.module.read
    private val write get() = world.module.write
    private val records get() = (world.module as ArchetypeEngineModule).records

    /**
     * Gets this entity's type (the ids of components added to it)
     * or throws an error if it is no longer active on the koinGet<Engine>().
     */
    val type: EntityType get() = records.getType(id)

    val children: EntityArray
        get() = queryManager.getEntitiesMatching(world.family {
            hasRelation(geary.components.childOf, this@Entity.id)
        }).toEntityArray(world)

    val instances: EntityArray
        get() = queryManager.getEntitiesMatching(world.family {
            hasRelation(geary.components.instanceOf, this@Entity.id)
        }).toEntityArray(world)

    val prefabs: List<Entity>
        get() = getRelations(geary.components.instanceOf, geary.components.any).map { it.target.toGeary(world) }

    /** Remove this entity from the ECS. */
    fun removeEntity() {
        entityRemoveProvider.remove(id)
    }

    /** Checks whether this entity has not been removed. */
    fun exists(): Boolean = read.exists(id)

    /**
     * Sets a component that holds data for this entity
     *
     * @param noEvent If true, will not fire a [OnAdd].
     */
    inline fun <reified T : Component> set(
        component: T,
        kClass: KClass<out T> = T::class,
        noEvent: Boolean = false,
    ): Unit = set(component, world.componentId(kClass), noEvent)

    fun set(
        component: Component,
        componentId: ComponentId,
        noEvent: Boolean = false,
    ): Unit = write.setComponentFor(id, componentId, component, noEvent)

    /** Sets components that hold data for this entity */
    fun setAll(components: Collection<Component>, override: Boolean = true) {
        components.forEach {
            if (override || !has(it::class)) set(it, it::class)
        }
    }

    /**
     * Adds a [component] to this entity's type, setting no data.
     *
     * @param noEvent If true, will not fire an [OnAdd] event.
     */
    fun add(component: ComponentId, noEvent: Boolean = false) {
        write.addComponentFor(id, component, noEvent)
    }

    /**
     * Adds the type [T] to this entity's type, setting no data.
     *
     * @param noEvent If true, will not fire an [OnAdd] event.
     */
    inline fun <reified T : Component> add(noEvent: Boolean = false) {
        add(world.componentId<T>(), noEvent)
    }

    /**
     * Adds a list of [components] to this entity's type, setting no data.
     *
     * @param noEvent If true, will not fire an [OnAdd] event.
     */
    fun addAll(components: Collection<ComponentId>, noEvent: Boolean = false) {
        components.forEach { add(it, noEvent) }
    }

    /**
     * Removes a component of type [T] from this entity.
     *
     * @return Whether the component was present before removal.
     */
    inline fun <reified T : Component> remove(noEvent: Boolean = false): Boolean =
        remove(world.componentId<T>(), noEvent)

    /** Removes a component whose class is [kClass] from this entity. */
    fun remove(kClass: KClass<*>, noEvent: Boolean = false): Boolean =
        remove(world.componentId(kClass), noEvent)

    /** Removes a component with id [component] from this entity. */
    fun remove(component: ComponentId, noEvent: Boolean = false): Boolean =
        write.removeComponentFor(id, component, noEvent)

    /**
     * Removes a list of [components] from this entity.
     *
     * @see remove
     */
    fun removeAll(components: Collection<ComponentId>): Boolean =
        components.any { remove(it) }

    /** Clears all components on this entity. */
    fun clear() {
        write.clearEntity(id)
    }

    /** Gets a component of type [T] on this entity. */
    inline fun <reified T : Component> get(): T? = get(T::class)

    /** @see get */
    inline fun <reified T : Component> get(kClass: KClass<out T>): T? =
        get(world.componentId(kClass)) as? T

    /** Gets a [component] which holds data from this entity. Use [has] if the component is not to hold data. */
    fun get(component: ComponentId): Component? =
        read.get(id, component)

    /** Gets a component of type [T] or sets a [default] if no component was present. */
    inline fun <reified T : Component> getOrSet(
        kClass: KClass<out T> = T::class,
        default: () -> T,
    ): T = get(kClass) ?: default().also { set(it) }

    /** Gets all the components on this entity, as well as relations in the form of [RelationComponent]. */
    fun getAll(): Set<Component> = read.getAll(id).toSet()

    /**
     * Checks whether this entity is an instance of another [entity]
     * (the other is the prefab this entity was made from).
     */
    fun instanceOf(entity: Entity): Boolean = has(Relation.of(geary.components.instanceOf, entity.id).id)

    /** Checks whether this entity has a component of type [T], regardless of it holding data. */
    inline fun <reified T : Component> has(): Boolean = has(T::class)

    /** @see has */
    inline fun <reified T : Component> has(kClass: KClass<out T>): Boolean =
        has(world.componentId(kClass))

    /** Checks whether this entity has a [component], regardless of it holding data. */
    fun has(component: ComponentId): Boolean =
        read.has(id, component)

    /**
     * Checks whether an entity has all of [components] set or added.
     *
     * @see has
     */
    fun hasAll(components: Collection<KClass<*>>): Boolean = components.all { has(it) }

    /** Adds a [base] entity to this entity.  */
    fun extend(base: Entity) {
        require(base.world == world)
        write.extendFor(id, base.id)
    }

    /** Adds a [prefab] entity to this entity.  */
    fun Entity.removePrefab(prefab: Entity) {
        remove(Relation.of(geary.components.instanceOf, prefab.id).id)
    }

    // Relations

    /** Gets the data stored under the relation of kind [K] and target [T]. */
    inline fun <reified K : Component, reified T : Component> getRelation(): K? {
        return getRelation(world.component<T>())
    }

    /** Gets the data stored under the relation of kind [K] and target [target]. */
    inline fun <reified K : Component> getRelation(target: Entity): K? {
        return get(Relation.of<K>(world, target).id) as? K
    }

    /** Like [getRelations], but reads appropriate data as requested and puts it in a [RelationWithData] object. */
    @Suppress("UNCHECKED_CAST") // Intrnal logic ensures cast always succeeds
    inline fun <reified K : Component?, reified T : Component?> getRelationsWithData(): List<RelationWithData<K, T>> =
        geary.read.getRelationsWithDataFor(
            id,
            world.componentIdWithNullable<K>(),
            world.componentIdWithNullable<T>()
        ) as List<RelationWithData<K, T>>

    fun getRelationsByKind(kind: ComponentId): List<Relation> =
        getRelations(kind, geary.components.any)

    /** Queries for relations using the same format as [AccessorOperations.getRelations]. */
    inline fun <reified K : Component?, reified T : Component?> getRelations(): List<Relation> =
        getRelations(world.componentIdWithNullable<K>(), world.componentIdWithNullable<T>())

    fun getRelations(kind: ComponentId, target: EntityId): List<Relation> =
        read.getRelationsFor(id, kind, target)

    inline fun <reified K : Component, reified T : Component> hasRelation(): Boolean =
        hasRelation<K>(world.component<T>())

    inline fun <reified K : Component?> hasRelation(target: Entity): Boolean =
        has(Relation.of<K>(world, target).id)

    inline fun <reified K : Any, reified T : Any> setRelation(data: K, noEvent: Boolean = false) {
        setRelation(data, world.component<T>(), noEvent)
    }

    inline fun <reified K : Any> setRelation(data: K, target: Entity, noEvent: Boolean = false) {
        setRelation(world.componentId<K>(), target.id, data, noEvent)
    }

    fun setRelation(kind: ComponentId, target: EntityId, data: Component, noEvent: Boolean = false) {
        geary.write.setComponentFor(id, Relation.of(kind, target).id, data, noEvent)
    }

    inline fun <reified K : Any, reified T : Any> addRelation(noEvent: Boolean = false) {
        addRelation<K>(world.component<T>(), noEvent)
    }

    inline fun <reified K : Any> addRelation(target: Entity, noEvent: Boolean = false) {
        geary.write.addComponentFor(id, Relation.of<K?>(world, target).id, noEvent)
    }

    fun addRelation(kind: ComponentId, target: EntityId, noEvent: Boolean = false) {
        geary.write.addComponentFor(id, Relation.of(kind, target).id, noEvent)
    }

    inline fun <reified K : Component, reified T : Component> removeRelation(noEvent: Boolean = false): Boolean {
        return removeRelation<K>(world.component<T>(), noEvent)
    }

    inline fun <reified K : Any> removeRelation(target: Entity, noEvent: Boolean = false): Boolean {
        return geary.write.removeComponentFor(id, Relation.of<K>(world, target).id, noEvent)
    }

    // Events
    inline fun <reified T : Any> emit(data: T? = null, involving: ComponentId = NO_COMPONENT) {
        emit(world.componentId<T>(), data, involving)
    }

    fun emit(event: ComponentId, data: Any? = null, involving: ComponentId = NO_COMPONENT) {
        geary.eventRunner.callEvent(event, data, involving, id)
    }

    // Prefabs

    /** @return All prefabs this entity is a deep instance of */
    fun collectPrefabs(): Set<GearyEntity> {
        return collectPrefabs(mutableSetOf(), listOf(this))
    }

    /** Checks whether this entity, or any depth of its prefabs is an instance of [prefab]. */
    fun deepInstanceOf(prefab: Entity): Boolean {
        return if (instanceOf(prefab)) return true
        else deepInstanceOf(mutableSetOf(), prefabs, prefab)
    }

    private tailrec fun collectPrefabs(collected: MutableSet<Entity>, search: List<Entity>): Set<GearyEntity> {
        if (search.isEmpty()) return collected
        val new = search.flatMap { it.prefabs } - collected
        collected.addAll(new)
        return collectPrefabs(collected, new)
    }

    private tailrec fun deepInstanceOf(seen: MutableSet<Entity>, search: List<Entity>, prefab: Entity): Boolean {
        if (search.isEmpty()) return false
        if (search.any { it.instanceOf(prefab) }) return true
        seen.addAll(search)
        return deepInstanceOf(seen, search.flatMap { it.prefabs } - seen, prefab)
    }

    // Other

    fun lookup(query: String): GearyEntity? {
        val firstSubstring = query.substringBefore(".")
        val child = children.firstOrNull {
            it.get<EntityName>()?.name == firstSubstring
        }
        val remaining = query.substringAfter(".")
        if (remaining == "" || remaining == query) return child
        return child?.lookup(remaining)
    }

    operator fun component1(): EntityId = id

    // Dangerous operations

    @DangerousComponentOperation
    fun set(components: Collection<Component>): Unit =
        set(component = components)

    // Marked for removal. Avoid breaking changes from adding the remove event.
    fun remove(kClass: KClass<*>): Boolean = remove(kClass, false)
    fun remove(component: ComponentId): Boolean = remove(component, false)

    @Deprecated(
        message = "Specify component type explicitly, otherwise the type may be inferred as Unit",
        level = DeprecationLevel.ERROR,
    )
    fun getOrSet(default: () -> Unit) {
        getOrSet<Unit> { }
    }
}
