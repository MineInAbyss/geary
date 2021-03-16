package com.mineinabyss.geary.ecs.api.entities

import com.mineinabyss.geary.ecs.api.ComponentClass
import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.GearyEntityId
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.components.*
import kotlin.reflect.KClass

/**
 * A wrapper around [GearyEntityId] that gets inlined to just a long (no performance degradation since no boxing occurs).
 * Provides some useful functions so we aren't forced to go through [Engine] every time we want to do some things.
 *
 * ### Note
 * Though inline classes can extend interfaces, the underlying type WILL NOT BE INLINED when we try to use methods from
 * said interface. Learn more [here](https://typealias.com/guides/inline-classes-and-autoboxing/).
 *
 * Thus, there is no longer support for implementing GearyEntity as other classes.
 */
@Suppress("NOTHING_TO_INLINE")
public inline class GearyEntity(public val id: GearyEntityId) {
    /** Remove this entity from the ECS. */
    public fun removeEntity() {
        Engine.removeEntity(id)
    }

    /** Sets a component that holds data for this entity */
    public inline fun <reified T : GearyComponent> set(component: T, kClass: KClass<out T> = T::class) {
        Engine.setComponentFor(id, componentId(kClass), component)
    }

    @Deprecated("Likely unintentionally using list as a single component", ReplaceWith("setAll()"))
    public fun set(components: Collection<GearyComponent>): Unit = set(component = components)

    /** Sets components that hold data for this entity */
    public fun setAll(components: Collection<GearyComponent>) {
        components.forEach { set(it, it::class) }
    }

    /** Adds a list of [component] to this entity */
    public inline fun add(component: GearyComponentId) {
        Engine.addComponentFor(id, component)
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
     * Ex. for bukkit entities this is done through a [PersistentDataContainer].
     */
    public inline fun setPersisting(component: GearyComponent) {
        set(component)
        //TODO persisting components should store a list of ComponentIDs
        //TODO is this possible to do nicely with traits?
        getOrSet { PersistingComponents() }.add(component)
    }

    @Deprecated("Likely unintentionally using list as a single component", ReplaceWith("setAllPersisting()"))
    public fun setPersisting(components: Collection<GearyComponent>): Unit = setPersisting(component = components)

    public inline fun setAllPersisting(components: Collection<GearyComponent>) {
        setAll(components)
        getOrSet { PersistingComponents() }.addAll(components)
    }

    /**
     * Removes a component of type [T] from this entity.
     *
     * @return Whether the component was present before removal.
     */
    public inline fun <reified T : GearyComponent> remove(): Boolean =
        remove(componentId<T>())

    public inline fun remove(kClass: ComponentClass): Boolean =
        remove(componentId(kClass))

    public inline fun remove(component: GearyComponentId): Boolean =
        Engine.removeComponentFor(id, component)

    public inline fun removeAll(components: Collection<GearyComponentId>): Boolean =
        components.any { remove(it) }

    /** Gets a component of type [T] on this entity. */
    public inline fun <reified T : GearyComponent> get(): T? =
        get(componentId<T>()) as? T

    /** Gets a [component] which holds data from this entity. Use [has] if the component is not to hold data. */
    public inline fun get(component: GearyComponentId): GearyComponent? =
        Engine.getComponentFor(id, component)

    /** Gets a component of type [T] or adds a [default] if no component was present. */
    public inline fun <reified T : GearyComponent> getOrSet(default: () -> T): T =
        get<T>() ?: default().also { set(it) }

    /** Gets a persisting component of type [T] or adds a [default] if no component was present. */
    public inline fun <reified T : GearyComponent> getOrSetPersisting(default: () -> T): T =
        get<T>() ?: default().also { setPersisting(id) }

    /** Gets all the active components on this entity. */
    public inline fun getComponents(): Set<GearyComponent> = Engine.getComponentsFor(id)

    /** Gets all the active persisting components on this entity. */
    public inline fun getPersistingComponents(): Set<GearyComponent> =
        get<PersistingComponents>()?.persisting?.intersect(getComponents()) ?: emptySet()

    //TODO update javadoc
    /** Gets all the active non-persisting components on this entity. */
    public inline fun getInstanceComponents(): Set<GearyComponent> =
        getComponents() - (get<PersistingComponents>()?.persisting ?: emptySet())

    /** Runs something on a component on this entity of type [T] if present. */
    public inline fun <reified T : GearyComponent> with(let: (T) -> Unit): Unit? = get<T>()?.let(let)

    /** Checks whether this entity has a component of type [T], regardless of whether or not it holds data. */
    public inline fun <reified T : GearyComponent> has(): Boolean = has(componentId<T>())

    /** Checks whether this entity has a [component], regardless of whether or not it holds data. */
    public inline fun has(component: GearyComponentId): Boolean =
        Engine.hasComponentFor(id, component)


    /** Checks whether an entity has all of a list of [components].
     * @see has */
    public inline fun hasAll(components: Collection<ComponentClass>): Boolean =
        components.all { has(componentId(it)) }

    public operator fun component1(): GearyEntityId = id
}
