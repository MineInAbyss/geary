package com.mineinabyss.geary.ecs.api.entities

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.GearyEntityId
import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.EngineContext
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.engine.temporaryEntity
import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.api.relations.RelationValueId
import com.mineinabyss.geary.ecs.api.systems.QueryContext
import com.mineinabyss.geary.ecs.api.systems.family
import com.mineinabyss.geary.ecs.components.PersistingComponent
import com.mineinabyss.geary.ecs.components.RelationComponent
import com.mineinabyss.geary.ecs.engine.*
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
    /** Gets the record associated with this entity or throws an error if it is no longer active on the koinGet<Engine>(). */
    context(EngineContext)
    @PublishedApi
    internal fun unsafeRecord(): Record = engine.unsafeRecord(this)

    /**
     * Gets this entity's type (the ids of components added to it)
     * or throws an error if it is no longer active on the koinGet<Engine>().
     */
    context(EngineContext)
    public val type: GearyType get() = unsafeRecord().archetype.type

    context(QueryContext)
    public val children: List<GearyEntity>
        get() = queryManager.getEntitiesMatching(family {
            has(id.withRole(CHILDOF))
        })

    context(QueryContext)
    public val instances: List<GearyEntity>
        get() = queryManager.getEntitiesMatching(family {
            has(id.withRole(INSTANCEOF))
        })

    /** Remove this entity from the ECS. */
    context(EngineContext)
    public fun removeEntity(callRemoveEvent: Boolean = true) {
        engine.removeEntity(this, callRemoveEvent)
    }

    /**
     * Sets a component that holds data for this entity
     *
     * @param noEvent If true, will not fire a [AddedComponent].
     */
    context(EngineContext)
    public inline fun <reified T : GearyComponent> set(
        component: T,
        kClass: KClass<out T> = T::class,
        noEvent: Boolean = false
    ): T {
        engine.setComponentFor(this, componentId(kClass), component, noEvent)
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
    context(EngineContext)
    public fun setAll(components: Collection<GearyComponent>, override: Boolean = true) {
        components.forEach {
            if (override || !has(it::class)) set(it, it::class)
        }
    }

    /** Gets the value of a relation with key of type [K] and value of type [V]. */
    context(EngineContext)
    public inline fun <reified K : GearyComponent, reified V : GearyComponent> getRelation(): V? =
        getRelation(K::class, V::class)

    /** Gets the value of a relation with key whose class is [key] and value of type [V]. */
    context(EngineContext)
    public inline fun <V : GearyComponent> getRelation(key: KClass<*>, value: KClass<V>): V? {
        @Suppress("UNCHECKED_CAST") // internally ensured to always be true
        return get(Relation.of(key, value).id) as? V
    }

    /** Gets the value of a relation with key whose class is [key] and value whose class is [value]. */
    context(EngineContext)
    public inline fun <reified V : Any> getRelation(key: GearyEntityId, value: KClass<V>): V? {
        return get(Relation.of(key, componentId(value)).id) as? V
    }

    /**
     * Sets a relation with key whose class is [keyClass] and a value [value],
     * optionally specifying the [valueClass].
     *
     * @param noEvent If true, will not fire an [AddedComponent] event.
     */
    context(EngineContext)
    public fun setRelation(
        keyClass: KClass<*>,
        value: Any,
        valueClass: KClass<*> = value::class,
        noEvent: Boolean = false
    ) {
        engine.setComponentFor(this, Relation.of(keyClass, valueClass).id, value, noEvent)
    }

    /**
     * Sets a relation with a [key] id and [value].
     * @param noEvent If true, will not fire an [AddedComponent] event.
     */
    context(EngineContext)
    public fun setRelation(key: GearyComponentId, value: Any, noEvent: Boolean = false) {
        engine.setComponentFor(this, Relation.of(key, componentId(value::class)).id, value, noEvent)
    }

    /** Removes a relation key key of type [K] and value of type [V]. */
    context(EngineContext)
    public inline fun <reified K : GearyComponent, reified V : GearyComponent> removeRelation(): Boolean =
        removeRelation(Relation.of<K, V>())

    /** Removes a specific [relation] from the entity. */
    context(EngineContext)
    public fun removeRelation(relation: Relation): Boolean =
        remove(Relation.of(relation.key, relation.value).id)

    /** Removes all relations with a value of type [V] on the entity. */
    context(EngineContext)
    public inline fun <reified V : GearyComponent> removeRelationsByValue(): Set<GearyComponent> =
        removeRelationsByValue(componentId<V>())

    /** Removes all relations with a value with id [componentId] on the entity. */
    context(EngineContext)
    public fun removeRelationsByValue(componentId: GearyComponentId): Set<GearyComponent> {
        val comps = engine.getRelationsFor(this, RelationValueId(componentId))
        comps.forEach { (_, relation) ->
            removeRelation(relation)
        }
        return comps.mapTo(mutableSetOf()) { it.first }
    }

    /**
     * Adds a [component] to this entity's type, setting no data.
     *
     * @param noEvent If true, will not fire an [AddedComponent] event.
     */
    context(EngineContext)
    public inline fun add(component: GearyComponentId, noEvent: Boolean = false) {
        engine.addComponentFor(this, component, noEvent)
    }

    /**
     * Adds the type [T] to this entity's type, setting no data.
     *
     * @param noEvent If true, will not fire an [AddedComponent] event.
     */
    context(EngineContext)
    public inline fun <reified T : GearyComponent> add(noEvent: Boolean = false) {
        add(componentId<T>(), noEvent)
    }

    /**
     * Adds a list of [components] to this entity's type, setting no data.
     *
     * @param noEvent If true, will not fire an [AddedComponent] event.
     */
    context(EngineContext)
    public inline fun addAll(components: Collection<GearyComponentId>, noEvent: Boolean = false) {
        components.forEach { add(it, noEvent) }
    }

    /**
     * Sets a persisting [component] on this entity, which will be serialized if possible.
     *
     * @param noEvent If true, will not fire an [AddedComponent] event.
     */
    context(EngineContext)
    public inline fun <reified T : GearyComponent> setPersisting(
        component: T,
        kClass: KClass<out T> = T::class,
        noEvent: Boolean = false
    ): T {
        set(component, kClass, noEvent)
        setRelation(kClass, PersistingComponent(), noEvent = noEvent)
        return component
    }

    /** Stops a given component on this entity from being persisted if it is already marked persistent. */
    context(EngineContext)
    public inline fun <reified T : GearyComponent> noPersist(
        kClass: KClass<out T> = T::class,
    ) {
        removeRelation(Relation.of(kClass, PersistingComponent::class))
    }

    context(EngineContext)
    @Deprecated("Likely unintentionally using list as a single component", ReplaceWith("setAllPersisting()"))
    public fun setPersisting(components: Collection<GearyComponent>): Collection<GearyComponent> =
        setPersisting(component = components)

    /**
     * Sets a list of persisting [components] on this entity.
     *
     * @param noEvent If true, will not fire an [AddedComponent] event.
     * @see setPersisting
     */
    context(EngineContext)
    public inline fun setAllPersisting(
        components: Collection<GearyComponent>,
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
    context(EngineContext)
    public inline fun <reified T : GearyComponent> remove(): Boolean =
        remove(componentId<T>()) || remove(componentId<T>() and ENTITY_MASK)

    /** Removes a component whose class is [kClass] from this entity. */
    context(EngineContext)
    public inline fun remove(kClass: KClass<*>): Boolean =
        remove(componentId(kClass))

    /** Removes a component with id [component] from this entity. */
    context(EngineContext)
    public inline fun remove(component: GearyComponentId): Boolean =
        engine.removeComponentFor(this, component)

    /**
     * Removes a list of [components] from this entity.
     *
     * @see remove
     */
    context(EngineContext)
    public inline fun removeAll(components: Collection<GearyComponentId>): Boolean =
        components.any { remove(it) }

    /** Clears all components on this entity. */
    context(EngineContext)
    public fun clear() {
        engine.clearEntity(this)
    }

    /** Gets a component of type [T] on this entity. */
    context(EngineContext)
    public inline fun <reified T : GearyComponent> get(kClass: KClass<out T> = T::class): T? =
        get(componentId(kClass)) as? T

    /** Gets a [component] which holds data from this entity. Use [has] if the component is not to hold data. */
    context(EngineContext)
    public inline fun get(component: GearyComponentId): GearyComponent? =
        engine.getComponentFor(this, component)

    /** Gets a component of type [T] or sets a [default] if no component was present. */
    context(EngineContext)
    public inline fun <reified T : GearyComponent> getOrSet(
        kClass: KClass<out T> = T::class,
        default: () -> T
    ): T =
        get(kClass) ?: default().also { set(it) }

    /** Gets a persisting component of type [T] or adds a [default] if no component was present. */
    context(EngineContext)
    public inline fun <reified T : GearyComponent> getOrSetPersisting(
        kClass: KClass<out T> = T::class,
        default: () -> T
    ): T = get(kClass) ?: default().also { setPersisting(it, kClass) }

    /** Gets all the components on this entity, as well as relations in the form of [RelationComponent]. */
    context(EngineContext)
    public fun getComponents(): Set<GearyComponent> = engine.getComponentsFor(this).toSet()

    /** Gets the data in any relations on this entity with a value of type [T]. */
    context(EngineContext)
    public inline fun <reified T : GearyComponent> getRelationsByValue(): Set<GearyComponent> =
        getRelationsByValue(RelationValueId(componentId<T>()))

    /** Gets the data in any relations on this entity with the value [relationValueId]. */
    context(EngineContext)
    public inline fun getRelationsByValue(relationValueId: RelationValueId): Set<GearyComponent> =
        engine.getRelationsFor(this, relationValueId).mapTo(mutableSetOf()) { it.first }

    /** Gets all persisting components on this entity. */
    context(EngineContext)
    public inline fun getPersistingComponents(): Set<GearyComponent> =
        getRelationsByValue(RelationValueId(componentId<PersistingComponent>()))

    /** Gets all non-persisting components on this entity. */
    context(EngineContext)
    public inline fun getInstanceComponents(): Set<GearyComponent> =
        getComponents() - getPersistingComponents()

    /**
     * Checks whether this entity is an instance of another [entity]
     * (the other is the prefab this entity was made from).
     */
    context(EngineContext)
    public inline fun instanceOf(entity: GearyEntity): Boolean =
        has(entity.id.withRole(INSTANCEOF))

    /** Checks whether this entity has a component of type [T], regardless of it holding data. */
    context(EngineContext)
    public inline fun <reified T : GearyComponent> has(kClass: KClass<out T> = T::class): Boolean =
        has(componentId(kClass))

    /** Checks whether this entity has a [component], regardless of it holding data. */
    context(EngineContext)
    public inline fun has(component: GearyComponentId): Boolean =
        engine.hasComponentFor(this, component)

    /**
     * Checks whether an entity has all of [components] set or added.
     *
     * @see has
     */
    context(EngineContext)
    public inline fun hasAll(components: Collection<KClass<*>>): Boolean = components.all { has(it) }

    /**
     * Calls an event with [components] attached to it and this entity as the target,
     * calculating a [result] after all handlers have run.
     */
    context(EngineContext)
    public inline fun <T> callEvent(
        vararg components: Any,
        source: GearyEntity? = null,
        crossinline result: (event: GearyEntity) -> T
    ): T = callEvent({
        setAll(components.toList())
    }, source = source, result = result)

    /** Calls an event with [components] attached to it and this entity as the target. */
    context(EngineContext)
    public inline fun callEvent(vararg components: Any, source: GearyEntity? = null): Unit =
        callEvent(source = source) {
            setAll(components.toList())
        }

    /** Calls an event on this entity using a temporary entity that can be configured with [initEvent]. */
    context(EngineContext)
    public inline fun callEvent(
        source: GearyEntity? = null,
        crossinline initEvent: GearyEntity.() -> Unit,
    ): Unit = callEvent(initEvent, source) {}

    /**
     * Calls an event on this entity using a temporary entity that can be configured with [init],
     * calculating a [result] after all handlers have run.
     */
    context(EngineContext)
    public inline fun <T> callEvent(
        crossinline init: GearyEntity.() -> Unit,
        source: GearyEntity? = null,
        crossinline result: (event: GearyEntity) -> T,
    ): T {
        return unsafeRecord().run {
            temporaryEntity(callRemoveEvent = false) { event ->
                init(event)
                callEvent(event, source)
                result(event)
            }
        }
    }

    /** Calls an event using a specific [entity][event] on this entity. */
    context(EngineContext)
    public fun callEvent(event: GearyEntity, source: GearyEntity? = null) {
        val (arc, row) = unsafeRecord()
        return arc.callEvent(event, row, source)
    }

    public operator fun component1(): GearyEntityId = id
}
