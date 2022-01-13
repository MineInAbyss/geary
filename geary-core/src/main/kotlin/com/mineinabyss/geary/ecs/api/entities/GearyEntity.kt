package com.mineinabyss.geary.ecs.api.entities

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.GearyEntityId
import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.engine.temporaryEntity
import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.api.relations.RelationValueId
import com.mineinabyss.geary.ecs.components.PersistingComponent
import com.mineinabyss.geary.ecs.components.RelationComponent
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
        Engine.removeEntity(this)
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
        Engine.setComponentFor(this, componentId(kClass), component, noEvent)
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

    /** Gets the value of a relation with key of type [K] and value of type [V]. */
    public inline fun <reified K : GearyComponent, reified V : GearyComponent> getRelation(): V? =
        getRelation(K::class, V::class)

    /** Gets the value of a relation with key whose class is [key] and value of type [V]. */
    public inline fun <V : GearyComponent> getRelation(key: KClass<*>, value: KClass<V>): V? {
        @Suppress("UNCHECKED_CAST") // internally ensured to always be true
        return get(Relation.of(key, value).id) as? V
    }

    /** Gets the value of a relation with key whose class is [key] and value whose class is [value]. */
    public inline fun <reified V : Any> getRelation(key: GearyEntityId, value: KClass<V>): V? {
        return get(Relation.of(key, componentId(value)).id) as? V
    }

    /**
     * Sets a relation with key whose class is [keyClass] and a value [value],
     * optionally specifying the [valueClass].
     *
     * @param noEvent If true, will not fire an [AddedComponent] event.
     */
    public fun setRelation(
        keyClass: KClass<*>,
        value: Any,
        valueClass: KClass<*> = value::class,
        noEvent: Boolean = false
    ) {
        Engine.setComponentFor(this, Relation.of(keyClass, valueClass).id, value, noEvent)
    }

    /**
     * Sets a relation with a [key] id and [value].
     * @param noEvent If true, will not fire an [AddedComponent] event.
     */
    public fun setRelation(key: GearyComponentId, value: Any, noEvent: Boolean = false) {
        Engine.setComponentFor(this, Relation.of(key, componentId(value::class)).id, value, noEvent)
    }

    /** Removes a relation key key of type [K] and value of type [V]. */
    public inline fun <reified K : GearyComponent, reified V : GearyComponent> removeRelation(): Boolean =
        removeRelation(Relation.of<K, V>())

    /** Removes a specific [relation] from the entity. */
    public fun removeRelation(relation: Relation): Boolean =
        remove(Relation.of(relation.key, relation.value).id)

    /**
     * Adds a [component] to this entity's type, setting no data.
     *
     * @param noEvent If true, will not fire an [AddedComponent] event.
     */
    public inline fun add(component: GearyComponentId, noEvent: Boolean = false) {
        Engine.addComponentFor(this, component, noEvent)
    }

    /**
     * Adds the type [T] to this entity's type, setting no data.
     *
     * @param noEvent If true, will not fire an [AddedComponent] event.
     */
    public inline fun <reified T : GearyComponent> add(noEvent: Boolean = false) {
        add(componentId<T>(), noEvent)
    }

    /**
     * Adds a list of [components] to this entity's type, setting no data.
     *
     * @param noEvent If true, will not fire an [AddedComponent] event.
     */
    public inline fun addAll(components: Collection<GearyComponentId>, noEvent: Boolean = false) {
        components.forEach { add(it, noEvent) }
    }

    /**
     * Sets a persisting [component] on this entity, which will be serialized if possible.
     *
     * @param noEvent If true, will not fire an [AddedComponent] event.
     */
    public inline fun <reified T : GearyComponent> setPersisting(
        component: T,
        kClass: KClass<out T> = T::class,
        noEvent: Boolean = false
    ): T {
        set(component, kClass, noEvent)
        setRelation(kClass, PersistingComponent(), noEvent = noEvent)
        return component
    }

    @Deprecated("Likely unintentionally using list as a single component", ReplaceWith("setAllPersisting()"))
    public fun setPersisting(components: Collection<GearyComponent>): Collection<GearyComponent> =
        setPersisting(component = components)

    /**
     * Sets a list of persisting [components] on this entity.
     *
     * @param noEvent If true, will not fire an [AddedComponent] event.
     * @see setPersisting
     */
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
    public inline fun <reified T : GearyComponent> remove(): Boolean =
        remove(componentId<T>()) || remove(componentId<T>() and ENTITY_MASK)

    /** Removes a component whose class is [kClass] from this entity. */
    public inline fun remove(kClass: KClass<*>): Boolean =
        remove(componentId(kClass))

    /** Removes a component with id [component] from this entity. */
    public inline fun remove(component: GearyComponentId): Boolean =
        Engine.removeComponentFor(this, component)

    /**
     * Removes a list of [components] from this entity.
     *
     * @see remove
     */
    public inline fun removeAll(components: Collection<GearyComponentId>): Boolean =
        components.any { remove(it) }

    /** Clears all components on this entity. */
    public fun clear() {
        Engine.clearEntity(this)
    }

    /** Gets a component of type [T] on this entity. */
    public inline fun <reified T : GearyComponent> get(kClass: KClass<out T> = T::class): T? =
        get(componentId(kClass)) as? T

    /** Gets a [component] which holds data from this entity. Use [has] if the component is not to hold data. */
    public inline fun get(component: GearyComponentId): GearyComponent? =
        Engine.getComponentFor(this, component)

    /** Gets a component of type [T] or sets a [default] if no component was present. */
    public inline fun <reified T : GearyComponent> getOrSet(kClass: KClass<out T> = T::class, default: () -> T): T =
        get(kClass) ?: default().also { set(it) }

    /** Gets a persisting component of type [T] or adds a [default] if no component was present. */
    public inline fun <reified T : GearyComponent> getOrSetPersisting(
        kClass: KClass<out T> = T::class,
        default: () -> T
    ): T = get(kClass) ?: default().also { setPersisting(it, kClass) }

    /** Gets all the components on this entity, as well as relations in the form of [RelationComponent]. */
    public inline fun getComponents(): Set<GearyComponent> = Engine.getComponentsFor(this)

    /** Gets the data in any relations on this entity with a value of type [T]. */
    public inline fun <reified T : GearyComponent> getRelationsByValue(): Set<GearyComponent> =
        getRelationsByValue(RelationValueId(componentId<T>()))

    /** Gets the data in any relations on this entity with the value [relationValueId]. */
    public inline fun getRelationsByValue(relationValueId: RelationValueId): Set<GearyComponent> =
        Engine.getRelationsFor(this, relationValueId).mapTo(mutableSetOf()) { it.first }

    /** Gets all persisting components on this entity. */
    public inline fun getPersistingComponents(): Set<GearyComponent> =
        getRelationsByValue(RelationValueId(componentId<PersistingComponent>()))

    /** Gets all non-persisting components on this entity. */
    public inline fun getInstanceComponents(): Set<GearyComponent> =
        getComponents() - getPersistingComponents()

    /**
     * Checks whether this entity is an instance of another [entity]
     * (the other is the prefab this entity was made from).
     */
    public inline fun instanceOf(entity: GearyEntity): Boolean =
        has(entity.id.withRole(INSTANCEOF))

    /** Checks whether this entity has a component of type [T], regardless of it holding data. */
    public inline fun <reified T : GearyComponent> has(kClass: KClass<out T> = T::class): Boolean =
        has(componentId(kClass))

    /** Checks whether this entity has a [component], regardless of it holding data. */
    public inline fun has(component: GearyComponentId): Boolean =
        Engine.hasComponentFor(this, component)

    /**
     * Checks whether an entity has all of [components] set or added.
     *
     * @see has
     */
    public inline fun hasAll(components: Collection<KClass<*>>): Boolean = components.all { has(it) }

    /**
     * Calls an event with [components] attached to it and this entity as the target,
     * calculating a [result] after all handlers have run.
     */
    public inline fun <T> callEvent(
        vararg components: Any,
        source: GearyEntity? = null,
        result: (event: GearyEntity) -> T
    ): T = callEvent({
        setAll(components.toList())
    }, source = source, result = result)

    /** Calls an event with [components] attached to it and this entity as the target. */
    public inline fun callEvent(vararg components: Any, source: GearyEntity? = null) {
        callEvent(source = source) {
            setAll(components.toList())
        }
    }

    /** Calls an event using a specific [entity][event] on this entity. */
    public inline fun callEvent(event: GearyEntity, source: GearyEntity? = null) {
        record.apply {
            archetype.callEvent(event, row, source)
        }
    }

    /** Calls an event on this entity using a temporary entity that can be configured with [initEvent]. */
    public inline fun callEvent(
        source: GearyEntity? = null,
        initEvent: GearyEntity.() -> Unit,
    ) {
        callEvent(initEvent, source) {}
    }

    /**
     * Calls an event on this entity using a temporary entity that can be configured with [init],
     * calculating a [result] after all handlers have run.
     */
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

    /** Gets the record associated with this entity or throws an error if it is no longer active on the Engine. */
    public inline val record: Record get() = Engine.getRecord(this)

    /**
     * Gets this entity's type (the ids of components added to it)
     * or throws an error if it is no longer active on the Engine.
     */
    public inline val type: GearyType get() = record.archetype.type

    public operator fun component1(): GearyEntityId = id
}
