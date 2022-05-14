package com.mineinabyss.geary.datatypes

import com.mineinabyss.geary.annotations.optin.DangerousComponentOperation
import com.mineinabyss.geary.components.Persists
import com.mineinabyss.geary.components.RelationComponent
import com.mineinabyss.geary.components.events.AddedComponent
import com.mineinabyss.geary.context.globalContext
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.engine.Engine
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.temporaryEntity
import com.mineinabyss.geary.systems.accessors.RelationWithData
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.reflect.KClass
import kotlin.reflect.typeOf

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
    @PublishedApi
    internal fun getRecord(): Record = globalContext.engine.getRecord(this)

    /**
     * Gets this entity's type (the ids of components added to it)
     * or throws an error if it is no longer active on the koinGet<Engine>().
     */
    public val type: GearyType get() = getRecord().archetype.type

    public val children: List<GearyEntity>
        get() = globalContext.queryManager.getEntitiesMatching(family {
            has(id.withRole(CHILDOF))
        })

    public val instances: List<GearyEntity>
        get() = globalContext.queryManager.getEntitiesMatching(family {
            has(id.withRole(INSTANCEOF))
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
    public inline fun <reified T : GearyComponent> set(
        component: T,
        kClass: KClass<out T> = T::class,
        noEvent: Boolean = false
    ): T {
        globalContext.engine.setComponentFor(this, componentId(kClass), component, noEvent)
        return component
    }

    /** Sets components that hold data for this entity */
    public fun setAll(components: Collection<GearyComponent>, override: Boolean = true) {
        components.forEach {
            if (override || !has(it::class)) set(it, it::class)
        }
    }

    /**
     * Adds a [component] to this entity's type, setting no data.
     *
     * @param noEvent If true, will not fire an [AddedComponent] event.
     */
    public inline fun add(component: GearyComponentId, noEvent: Boolean = false) {
        globalContext.engine.addComponentFor(this, component, noEvent)
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
        setRelation(Persists(), componentId(kClass), noEvent)
        return component
    }

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
        globalContext.engine.removeComponentFor(this, component)

    /**
     * Removes a list of [components] from this entity.
     *
     * @see remove
     */
    public inline fun removeAll(components: Collection<GearyComponentId>): Boolean =
        components.any { remove(it) }

    /** Clears all components on this entity. */
    public fun clear() {
        globalContext.engine.clearEntity(this)
    }

    /** Gets a component of type [T] on this entity. */
    public inline fun <reified T : GearyComponent> get(kClass: KClass<out T> = T::class): T? =
        get(componentId(kClass)) as? T

    /** Gets a [component] which holds data from this entity. Use [has] if the component is not to hold data. */
    public inline fun get(component: GearyComponentId): GearyComponent? =
        globalContext.engine.getComponentFor(this, component)

    /** Gets a component of type [T] or sets a [default] if no component was present. */
    public inline fun <reified T : GearyComponent> getOrSet(
        kClass: KClass<out T> = T::class,
        default: () -> T
    ): T =
        get(kClass) ?: default().also { set(it) }

    /** Gets a persisting component of type [T] or adds a [default] if no component was present. */
    public inline fun <reified T : GearyComponent> getOrSetPersisting(
        kClass: KClass<out T> = T::class,
        default: () -> T
    ): T = get(kClass) ?: default().also { setPersisting(it, kClass) }

    /** Gets all the components on this entity, as well as relations in the form of [RelationComponent]. */
    public fun getComponents(): Set<GearyComponent> = globalContext.engine.getComponentsFor(this).toSet()

    /** Gets all persisting components on this entity. */
    public inline fun getPersistingComponents(): Set<GearyComponent> =
        getRelations<Persists, Any>().mapTo(mutableSetOf()) { it.target }

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
        globalContext.engine.hasComponentFor(this, component)

    /**
     * Checks whether an entity has all of [components] set or added.
     *
     * @see has
     */
    public inline fun hasAll(components: Collection<KClass<*>>): Boolean = components.all { has(it) }

    // Relations

    /** Gets the value of a relation with key of type [Y] and value of type [T]. */
    public inline fun <reified Y : GearyComponent, reified T : GearyComponent> getRelation(): Y? {
        return getRelation<Y>(componentId<T>())
    }

    public inline fun <reified Y : GearyComponent> getRelation(target: GearyEntityId): Y? {
        return get(Relation.of<Y>(target).id) as? Y
    }

    public inline fun <reified Y : GearyComponent, reified T : GearyComponent?> getRelations(): Set<RelationWithData<Y, T>> {
        val type = typeOf<Y>()
        return when {
            type.classifier == Any::class -> globalContext.engine.getRelationsByTypeFor(this, componentId<Y>(),)
            typeOf<T>() == typeOf<Any>() -> globalContext.engine.getRelationsByTargetFor(this, componentId<T>(),)
            else -> error("One of ${Y::class.simpleName} or ${T::class.simpleName} must be Any when getting relations.")
        } as Set<RelationWithData<Y, T>>
    }


    public inline fun <reified Y : GearyComponent, reified T : GearyComponent> hasRelation(): Boolean =
        hasRelation<Y>(componentId<T>())

    public inline fun <reified Y : GearyComponent> hasRelation(target: GearyEntityId): Boolean =
        has(Relation.of<Y>(target).id)

    public inline fun <reified Y : Any, reified T : Any> setRelation(data: Y, noEvent: Boolean = false) {
        setRelation<Y>(data, componentId<T>(), noEvent)
    }

    public inline fun <reified Y : Any> setRelation(data: Y, target: GearyEntityId, noEvent: Boolean = false) {
        globalContext.engine.setComponentFor(this, Relation.of<Y>(target).id, data, noEvent)
    }

    public inline fun <reified Y : Any, reified T : Any> addRelation(noEvent: Boolean = false) {
        addRelation<Y>(componentId<T>(), noEvent)
    }

    public inline fun <reified Y : Any> addRelation(target: GearyEntityId, noEvent: Boolean = false) {
        globalContext.engine.addComponentFor(this, Relation.of<Y>(target).id, noEvent)
    }

    /** Removes a relation key key of type [K] and value of type [V]. */
    public inline fun <reified Y : GearyComponent, reified T : GearyComponent> removeRelation(): Boolean {
        return removeRelation<Y>(componentId<T>())
    }

    public inline fun <reified Y : GearyComponent> removeRelation(target: GearyEntityId): Boolean {
        return globalContext.engine.removeComponentFor(this, Relation.of<Y>(target).id)
    }

    /** Removes a specific [relation] from the entity. */
    public fun removeRelation(relation: Relation): Boolean = remove(relation.id)

    // Events

    /**
     * Calls an event with [components] attached to it and this entity as the target,
     * calculating a [result] after all handlers have run.
     */
    public inline fun <T> callEvent(
        vararg components: Any,
        source: GearyEntity? = null,
        crossinline result: (event: GearyEntity) -> T
    ): T = callEvent({
        setAll(components.toList())
    }, source = source, result = result)

    /** Calls an event with [components] attached to it and this entity as the target. */
    public inline fun callEvent(vararg components: Any, source: GearyEntity? = null): Unit =
        callEvent(source = source) {
            setAll(components.toList())
        }

    /** Calls an event on this entity using a temporary entity that can be configured with [initEvent]. */
    public inline fun callEvent(
        source: GearyEntity? = null,
        crossinline initEvent: GearyEntity.() -> Unit,
    ): Unit = callEvent(initEvent, source) {}

    /**
     * Calls an event on this entity using a temporary entity that can be configured with [init],
     * calculating a [result] after all handlers have run.
     */
    public inline fun <T> callEvent(
        crossinline init: GearyEntity.() -> Unit,
        source: GearyEntity? = null,
        crossinline result: (event: GearyEntity) -> T,
    ): T {
        return getRecord().run {
            temporaryEntity(callRemoveEvent = false) { event ->
                init(event)
                callEvent(event, source)
                result(event)
            }
        }
    }

    /** Calls an event using a specific [entity][event] on this entity. */
    public fun callEvent(event: GearyEntity, source: GearyEntity? = null) {
        val (arc, row) = getRecord()
        return arc.callEvent(event, row, source)
    }

    public operator fun component1(): GearyEntityId = id

    // Dangerous operations

    @DangerousComponentOperation
    public fun set(components: Collection<GearyComponent>): Unit =
        TODO()

    @DangerousComponentOperation
    public fun setPersisting(components: Collection<GearyComponent>): Collection<GearyComponent> =
        setPersisting(component = components)

    @DangerousComponentOperation
    public fun <K : Any, V : Collection<Any?>> addRelation() {
        TODO()
    }

    @DangerousComponentOperation
    public fun <K : Any, V : Collection<Any?>> setRelation() {
        TODO()
    }

    @DangerousComponentOperation
    public fun <K : Any, V : Collection<Any?>> setRelation(value: Any?) {
        TODO()
    }
}
