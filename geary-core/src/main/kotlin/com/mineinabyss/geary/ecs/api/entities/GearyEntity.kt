package com.mineinabyss.geary.ecs.api.entities

import com.mineinabyss.geary.ecs.api.*
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.engine.temporaryEntity
import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.api.relations.RelationValueId
import com.mineinabyss.geary.ecs.components.PersistingComponent
import com.mineinabyss.geary.ecs.engine.ENTITY_MASK
import com.mineinabyss.geary.ecs.engine.INSTANCEOF
import com.mineinabyss.geary.ecs.engine.Record
import com.mineinabyss.geary.ecs.engine.withRole
import com.mineinabyss.geary.ecs.events.AddedComponent
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

/**
 * A wrapper around [GearyEntityId] that gets inlined to just a long (no performance degradation since no boxing occurs).
 *
 * Provides some useful functions so we aren't forced to go through [Engine] every time we want to do some things.
 */
@Serializable
@JvmInline
@Suppress("NOTHING_TO_INLINE")
public value class GearyEntity(public val id: GearyEntityId) {
    /** Remove this entity from the ECS. */
    public fun removeEntity() {
        Engine.removeEntity(id)
    }

    /**
     * Sets a component that holds data for this entity
     *
     * @param noEvent If true, will not fire a [AddedComponent].
     */
    public inline fun <reified T : GearyComponent> set(
        component: T,
        kClass: KClass<out T> = T::class,
        noEvent: Boolean = false
    ): T {
        Engine.setComponentFor(id, componentId(kClass), component, noEvent)
        return component
    }

    @Deprecated(
        "Likely unintentionally using list as a single component, use set<T: GearyComponent>() if this is intentional.",
        ReplaceWith("setAll()")
    )
    @Suppress("UNUSED_PARAMETER")
    public fun set(components: Collection<GearyComponent>): Unit =
        error("Trying to set a collection with set method instead of setAll")

    /** Sets components that hold data for this entity */
    public fun setAll(components: Collection<GearyComponent>, override: Boolean = true) {
        components.forEach {
            if (override || !has(it::class)) set(it, it::class)
        }
    }

    public inline fun <reified K : GearyComponent, reified V : GearyComponent> getRelation(): V? =
        getRelation(K::class, V::class)

    public inline fun <V : GearyComponent> getRelation(key: KClass<*>, value: KClass<V>): V? {
        @Suppress("UNCHECKED_CAST") // internally ensured to always be true
        return get(Relation.of(key, value).id) as? V
    }

    public inline fun <reified V : Any> getRelation(key: GearyEntityId, value: KClass<V>): V? {
        return get(Relation.of(key, componentId(value)).id) as? V
    }

    /**
     * @param noEvent If true, will not fire a [AddedComponent].
     */
    public fun setRelation(
        keyClass: KClass<*>,
        value: Any,
        valueClass: KClass<*> = value::class,
        noEvent: Boolean = false
    ) {
        Engine.setComponentFor(id, Relation.of(keyClass, valueClass).id, value, noEvent)
    }

    public fun setRelation(key: GearyComponentId, value: Any, noEvent: Boolean = false) {
        Engine.setComponentFor(id, Relation.of(key, componentId(value::class)).id, value, noEvent)
    }

    public inline fun <reified K : GearyComponent, reified V : GearyComponent> removeRelation(): Boolean =
        removeRelation(Relation.of<K, V>())

    public fun removeRelation(relation: Relation): Boolean =
        remove(Relation.of(relation.key, relation.value).id)

    /**
     * Adds a list of [component] to this entity
     *
     * @param noEvent If true, will not fire a [AddedComponent].
     */
    public inline fun add(component: GearyComponentId, noEvent: Boolean = false) {
        Engine.addComponentFor(id, component, noEvent)
    }

    public inline fun <reified T : GearyComponent> add() {
        add(componentId<T>())
    }

    public inline fun addAll(components: Collection<GearyComponentId>) {
        components.forEach { add(it) }
    }

    /**
     * Adds a persisting [component] to this entity, which will be serialized in some way if possible.
     *
     * Ex. for bukkit entities this is done through a PersistentDataContainer.
     */
    public inline fun <reified T : GearyComponent> setPersisting(component: T, kClass: KClass<out T> = T::class): T {
        set(component, kClass)
        setRelation(kClass, PersistingComponent())
        return component
    }

    @Deprecated("Likely unintentionally using list as a single component", ReplaceWith("setAllPersisting()"))
    public fun setPersisting(components: Collection<GearyComponent>): Collection<GearyComponent> =
        setPersisting(component = components)

    public inline fun setAllPersisting(components: Collection<GearyComponent>, override: Boolean = true) {
        components.forEach {
            if (override || !has(it::class)) setPersisting(it, it::class)
        }
    }

    /**
     * Removes a component of type [T] from this entity.
     *
     * @return Whether the component was present before removal.
     */
    public inline fun <reified T : GearyComponent> remove(): Boolean =
        remove(componentId<T>()) || remove(componentId<T>() and ENTITY_MASK)

    public inline fun remove(kClass: ComponentClass): Boolean =
        remove(componentId(kClass))

    public inline fun remove(component: GearyComponentId): Boolean =
        Engine.removeComponentFor(id, component)

    public inline fun removeAll(components: Collection<GearyComponentId>): Boolean =
        components.any { remove(it) }

    /** Clears the components on this entity. */
    public fun clear() {
        Engine.clearEntity(id)
    }

    /** Gets a component of type [T] on this entity. */
    public inline fun <reified T : GearyComponent> get(kClass: KClass<out T> = T::class): T? =
        get(componentId(kClass)) as? T

    /** Gets a [component] which holds data from this entity. Use [has] if the component is not to hold data. */
    public inline fun get(component: GearyComponentId): GearyComponent? =
        Engine.getComponentFor(id, component)

    /** Gets a component of type [T] or adds a [default] if no component was present. */
    public inline fun <reified T : GearyComponent> getOrSet(kClass: KClass<out T> = T::class, default: () -> T): T =
        get(kClass) ?: default().also { set(it) }

    /** Gets a persisting component of type [T] or adds a [default] if no component was present. */
    public inline fun <reified T : GearyComponent> getOrSetPersisting(
        kClass: KClass<out T> = T::class,
        default: () -> T
    ): T = get(kClass) ?: default().also { setPersisting(it, kClass) }

    /** Gets all the active components on this entity. */
    public inline fun getComponents(): Set<GearyComponent> = Engine.getComponentsFor(id)


    /** Removes all components related to a parent of type [T] on the entity. */
    public inline fun <reified T : GearyComponent> removeRelations(): Set<GearyComponent> {
        val comps = Engine.getRelationsFor(id, RelationValueId(componentId<T>()))
        comps.forEach { (_, relation) ->
            removeRelation(relation)
        }
        return comps.mapTo(mutableSetOf()) { it.first }
    }

    /** Gets a list of components related to the component represented by [T]. */
    public inline fun <reified T : GearyComponent> getComponentsRelatedTo(): Set<GearyComponent> =
        getComponentsRelatedTo(RelationValueId(componentId<T>()))

    /** Gets a list of components related to the component represented by [relationValueId]. */
    public inline fun getComponentsRelatedTo(relationValueId: RelationValueId): Set<GearyComponent> =
        Engine.getRelationsFor(id, relationValueId).mapTo(mutableSetOf()) { it.first }

    /** Gets all the active persisting components on this entity. */
    public inline fun getPersistingComponents(): Set<GearyComponent> =
        getComponentsRelatedTo(RelationValueId(componentId<PersistingComponent>()))

    //TODO update javadoc
    /** Gets all the active non-persisting components on this entity. */
    public inline fun getInstanceComponents(): Set<GearyComponent> =
        getComponents() - getPersistingComponents()

    public inline fun instanceOf(entity: GearyEntity): Boolean =
        has(entity.id.withRole(INSTANCEOF))

    /** Checks whether this entity has a component of type [T], regardless of whether or not it holds data. */
    public inline fun <reified T : GearyComponent> has(kClass: KClass<out T> = T::class): Boolean =
        has(componentId(kClass))

    /** Checks whether this entity has a [component], regardless of whether or not it holds data. */
    public inline fun has(component: GearyComponentId): Boolean =
        Engine.hasComponentFor(id, component)

    /** Checks whether an entity has all of a list of [components].
     * @see has */
    public inline fun hasAll(components: Collection<ComponentClass>): Boolean = components.all { has(it) }

    public inline fun <T> callEvent(
        vararg components: Any,
        source: GearyEntity? = null,
        result: (event: GearyEntity) -> T
    ): T = callEvent({
        setAll(components.toList())
    }, source = source, result = result)

    public inline fun callEvent(vararg components: Any, source: GearyEntity? = null) {
        callEvent(source = source) {
            setAll(components.toList())
        }
    }

    public inline fun callEvent(event: GearyEntity, source: GearyEntity) {
        record.apply {
            archetype.callEvent(event, row, source)
        }
    }

    public inline fun callEvent(
        source: GearyEntity? = null,
        initEvent: GearyEntity.() -> Unit,
    ) {
        callEvent(initEvent, source) {}
    }

    public inline fun <T> callEvent(
        init: GearyEntity.() -> Unit,
        source: GearyEntity? = null,
        result: (event: GearyEntity) -> T,
    ): T {
        record.apply {
            Engine.temporaryEntity { event ->
                init(event)
                archetype.callEvent(event, row, source)
                return result(event)
            }
        }
        error("Failed to get an entity while calling event that expects a result returned")
    }

    public inline val record: Record get() = Engine.getRecord(id)
    public inline val type: GearyType get() = record.archetype.type

    public operator fun component1(): GearyEntityId = id
}
