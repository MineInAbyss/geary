package com.mineinabyss.geary.datatypes

import com.mineinabyss.geary.annotations.optin.DangerousComponentOperation
import com.mineinabyss.geary.components.events.AddedComponent
import com.mineinabyss.geary.components.relations.ChildOf
import com.mineinabyss.geary.components.relations.InstanceOf
import com.mineinabyss.geary.components.relations.Persists
import com.mineinabyss.geary.context.globalContext
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.engine.Engine
import com.mineinabyss.geary.helpers.component
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.componentIdWithNullable
import com.mineinabyss.geary.helpers.temporaryEntity
import com.mineinabyss.geary.systems.accessors.AccessorOperations
import com.mineinabyss.geary.systems.accessors.RelationWithData
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.reflect.KClass

/**
 * A wrapper around [EntityId] that gets inlined to just a long (no performance degradation since no boxing occurs).
 *
 * Provides some useful functions so we aren't forced to go through [Engine] every time we want to do some things.
 */
@Serializable
@JvmInline
@Suppress("NOTHING_TO_INLINE")
public value class Entity(public val id: EntityId) {
    /**
     * Gets this entity's type (the ids of components added to it)
     * or throws an error if it is no longer active on the koinGet<Engine>().
     */
    public val type: EntityType get() = globalContext.engine.getType(this)

    public val children: List<Entity>
        get() = globalContext.queryManager.getEntitiesMatching(family {
            hasRelation<ChildOf?>(this@Entity)
        })

    public val instances: List<Entity>
        get() = globalContext.queryManager.getEntitiesMatching(family {
            hasRelation<InstanceOf?>(this@Entity)
        })

    /** Remove this entity from the ECS. */
    public fun removeEntity(callRemoveEvent: Boolean = true) {
        globalContext.engine.removeEntity(this, callRemoveEvent)
    }

    /**
     * Sets a component that holds data for this entity
     *
     * @param noEvent If true, will not fire a [AddedComponent].
     */
    public inline fun <reified T : Component> set(
        component: T,
        kClass: KClass<out T> = T::class,
        noEvent: Boolean = false
    ): Unit = set(component, componentId(kClass), noEvent)

    public inline fun set(
        component: Component,
        componentId: ComponentId,
        noEvent: Boolean = false
    ): Unit = globalContext.engine.setComponentFor(this, componentId, component, noEvent)

    /** Sets components that hold data for this entity */
    public fun setAll(components: Collection<Component>, override: Boolean = true) {
        components.forEach {
            if (override || !has(it::class)) set(it, it::class)
        }
    }

    /**
     * Adds a [component] to this entity's type, setting no data.
     *
     * @param noEvent If true, will not fire an [AddedComponent] event.
     */
    public inline fun add(component: ComponentId, noEvent: Boolean = false) {
        globalContext.engine.addComponentFor(this, component, noEvent)
    }

    /**
     * Adds the type [T] to this entity's type, setting no data.
     *
     * @param noEvent If true, will not fire an [AddedComponent] event.
     */
    public inline fun <reified T : Component> add(noEvent: Boolean = false) {
        add(componentId<T>(), noEvent)
    }

    /**
     * Adds a list of [components] to this entity's type, setting no data.
     *
     * @param noEvent If true, will not fire an [AddedComponent] event.
     */
    public inline fun addAll(components: Collection<ComponentId>, noEvent: Boolean = false) {
        components.forEach { add(it, noEvent) }
    }

    /**
     * Sets a persisting [component] on this entity, which will be serialized if possible.
     *
     * @param noEvent If true, will not fire an [AddedComponent] event.
     */
    public inline fun <reified T : Component> setPersisting(
        component: T,
        kClass: KClass<out T> = T::class,
        noEvent: Boolean = false
    ): T {
        set(component, kClass, noEvent)
        setRelation(Persists(), component(kClass), noEvent)
        return component
    }

    /**
     * Sets a list of persisting [components] on this entity.
     *
     * @param noEvent If true, will not fire an [AddedComponent] event.
     * @see setPersisting
     */
    public inline fun setAllPersisting(
        components: Collection<Component>,
        override: Boolean = true,
        noEvent: Boolean = false
    ) {
        components.forEach {
            if (override || !has(it::class)) setPersisting(it, it::class, noEvent)
        }
    }

    /**
     * Removes a component of type [T] from this entity.
     *
     * @return Whether the component was present before removal.
     */
    public inline fun <reified T : Component> remove(): Boolean =
        remove(componentId<T>()) || remove(componentId<T>() and ENTITY_MASK)

    /** Removes a component whose class is [kClass] from this entity. */
    public inline fun remove(kClass: KClass<*>): Boolean =
        remove(componentId(kClass))

    /** Removes a component with id [component] from this entity. */
    public inline fun remove(component: ComponentId): Boolean =
        globalContext.engine.removeComponentFor(this, component)

    /**
     * Removes a list of [components] from this entity.
     *
     * @see remove
     */
    public inline fun removeAll(components: Collection<ComponentId>): Boolean =
        components.any { remove(it) }

    /** Clears all components on this entity. */
    public fun clear() {
        globalContext.engine.clearEntity(this)
    }

    /** Gets a component of type [T] on this entity. */
    public inline fun <reified T : Component> get(): T? = get(T::class)

    /** @see get */
    public inline fun <reified T : Component> get(kClass: KClass<out T>): T? =
        get(componentId(kClass)) as? T

    /** Gets a [component] which holds data from this entity. Use [has] if the component is not to hold data. */
    public inline fun get(component: ComponentId): Component? =
        globalContext.engine.getComponentFor(this, component)

    /** Gets a component of type [T] or sets a [default] if no component was present. */
    public inline fun <reified T : Component> getOrSet(
        kClass: KClass<out T> = T::class,
        default: () -> T
    ): T = get(kClass) ?: default().also { set(it) }

    /** Gets a persisting component of type [T] or adds a [default] if no component was present. */
    public inline fun <reified T : Component> getOrSetPersisting(
        kClass: KClass<out T> = T::class,
        default: () -> T
    ): T = get(kClass) ?: default().also { setPersisting(it, kClass) }

    /** Gets all the components on this entity, as well as relations in the form of [RelationComponent]. */
    public fun getAll(): Set<Component> = globalContext.engine.getComponentsFor(this).toSet()

    /** Gets all persisting components on this entity. */
    public inline fun getAllPersisting(): Set<Component> =
        getRelationsWithData<Persists, Any>().mapTo(mutableSetOf()) { it.targetData }

    /** Gets all non-persisting components on this entity. */
    public inline fun getAllNotPersisting(): Set<Component> =
        getAll() - getAllPersisting()

    /**
     * Checks whether this entity is an instance of another [entity]
     * (the other is the prefab this entity was made from).
     */
    public inline fun instanceOf(entity: Entity): Boolean =
        hasRelation<InstanceOf>(entity)

    /** Checks whether this entity has a component of type [T], regardless of it holding data. */
    public inline fun <reified T : Component> has(): Boolean = has(T::class)

    /** @see has */
    public inline fun <reified T : Component> has(kClass: KClass<out T>): Boolean =
        has(componentId(kClass))


    /** Checks whether this entity has a [component], regardless of it holding data. */
    public inline fun has(component: ComponentId): Boolean =
        globalContext.engine.hasComponentFor(this, component)

    /**
     * Checks whether an entity has all of [components] set or added.
     *
     * @see has
     */
    public inline fun hasAll(components: Collection<KClass<*>>): Boolean = components.all { has(it) }

    // Relations

    /** Gets the data stored under the relation of kind [K] and target [T]. */
    public inline fun <reified K : Component, reified T : Component> getRelation(): K? {
        return getRelation(component<T>())
    }

    /** Gets the data stored under the relation of kind [K] and target [target]. */
    public inline fun <reified K : Component> getRelation(target: Entity): K? {
        return get(Relation.of<K>(target).id) as? K
    }

    /** Like [getRelations], but reads appropriate data as requested and puts it in a [RelationWithData] object. */
    @Suppress("UNCHECKED_CAST") // Intrnal logic ensures cast always succeeds
    public inline fun <reified K : Component?, reified T : Component?> getRelationsWithData(): List<RelationWithData<K, T>> =
        globalContext.engine.getRelationsWithDataFor(
            this,
            componentIdWithNullable<K>(),
            componentIdWithNullable<T>()
        ) as List<RelationWithData<K, T>>

    /** Queries for relations using the same format as [AccessorOperations.getRelations]. */
    public inline fun <reified K : Component?, reified T : Component?> getRelations(): List<Relation> =
        getRelations(componentIdWithNullable<K>(), componentIdWithNullable<T>())

    public fun getRelations(kind: ComponentId, target: EntityId): List<Relation> =
        globalContext.engine.getRelationsFor(this, kind, target)

    public inline fun <reified K : Component, reified T : Component> hasRelation(): Boolean =
        hasRelation<K>(component<T>())

    public inline fun <reified K : Component> hasRelation(target: Entity): Boolean =
        has(Relation.of<K>(target).id)

    public inline fun <reified K : Any, reified T : Any> setRelation(data: K, noEvent: Boolean = false) {
        setRelation(data, component<T>(), noEvent)
    }

    public inline fun <reified K : Any> setRelation(data: K, target: Entity, noEvent: Boolean = false) {
        globalContext.engine.setComponentFor(this, Relation.of<K>(target).id, data, noEvent)
    }

    public inline fun <reified K : Any, reified T : Any> addRelation(noEvent: Boolean = false) {
        addRelation<K>(component<T>(), noEvent)
    }

    public inline fun <reified K : Any> addRelation(target: Entity, noEvent: Boolean = false) {
        globalContext.engine.addComponentFor(this, Relation.of<K?>(target).id, noEvent)
    }

    /** Removes a relation key of type [K] and value of type [V]. */
    public inline fun <reified K : Component, reified T : Component> removeRelation(): Boolean {
        return removeRelation<K>(component<T>())
    }

    public inline fun <reified K : Any> removeRelation(target: Entity): Boolean {
        return globalContext.engine.removeComponentFor(this, Relation.of<K>(target).id)
    }

    // Events

    /**
     * Calls an event with [components] attached to it and this entity as the target,
     * calculating a [result] after all handlers have run.
     */
    public inline fun <T> callEvent(
        vararg components: Any,
        source: Entity? = null,
        crossinline result: (event: Entity) -> T
    ): T = callEvent({
        setAll(components.toList())
    }, source = source, result = result)

    /** Calls an event with [components] attached to it and this entity as the target. */
    public inline fun callEvent(vararg components: Any, source: Entity? = null): Unit =
        callEvent(source = source) {
            setAll(components.toList())
        }

    /** Calls an event on this entity using a temporary entity that can be configured with [initEvent]. */
    public inline fun callEvent(
        source: Entity? = null,
        crossinline initEvent: Entity.() -> Unit,
    ): Unit = callEvent(initEvent, source) {}

    /**
     * Calls an event on this entity using a temporary entity that can be configured with [init],
     * calculating a [result] after all handlers have run.
     */
    public inline fun <T> callEvent(
        crossinline init: Entity.() -> Unit,
        source: Entity? = null,
        crossinline result: (event: Entity) -> T,
    ): T {
        return temporaryEntity(callRemoveEvent = false) { event ->
            init(event)
            callEvent(event, source)
            result(event)
        }
    }

    /** Calls an event using a specific [entity][event] on this entity. */
    public fun callEvent(event: Entity, source: Entity? = null) {
        globalContext.engine.callEvent(this, event, source)
    }

    // Other

    public operator fun component1(): EntityId = id

    // Dangerous operations

    @DangerousComponentOperation
    public fun set(components: Collection<Component>): Unit =
        set(component = components)

    @DangerousComponentOperation
    public fun setPersisting(components: Collection<Component>): Collection<Component> =
        setPersisting(component = components)
}
