package com.mineinabyss.geary.ecs.api.entities

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.GearyComponentId
import com.mineinabyss.geary.ecs.GearyEntityId
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.components.*
import com.mineinabyss.geary.ecs.engine.componentId
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
    public fun remove() {
        Engine.removeEntity(id)
    }

    /** Adds a component of type [T] to this entity */
    public fun <T : GearyComponent> addComponent(component: T): T =
        Engine.addComponentFor(id, component)

    /** Adds a list of [components] to this entity */
    public inline fun addComponents(components: Collection<GearyComponent>) {
        Engine.addComponentsFor(id, components.toSet())
    }

    /** Adds a list of [components] to this entity */
    public inline fun addComponents(vararg components: GearyComponent) {
        addComponents(components.toSet())
    }

    /**
     * Adds a persisting [component] to this entity, which will be serialized in some way if possible.
     *
     * Ex. for bukkit entities this is done through a [PersistentDataContainer].
     */
    public inline fun <reified T : GearyComponent> addPersistingComponent(component: T): T {
        addComponent(component)
        getOrAdd { PersistingComponents() }.add(component)
        return component
    }

    /**
     * Adds a list of persisting [components]
     * @see addPersistingComponent
     */
    public inline fun addPersistingComponents(components: Set<GearyComponent>) {
        if (components.isEmpty()) return //avoid adding a persisting components component if there are none to add
        addComponents(components)
        getOrAdd { PersistingComponents() }.addAll(components)
    }

    /**
     * Removes a component of type [T] from this entity.
     *
     * @return Whether the component was present before removal.
     */
    public inline fun <reified T : GearyComponent> removeComponent(): Boolean =
        Engine.removeComponentFor(id, componentId<T>())

    /** Gets a component of type [T] or adds a [default] if no component was present. */
    public inline fun <reified T : GearyComponent> getOrAdd(default: () -> T): T =
        get<T>() ?: addComponent(default())

    /** Gets a persisting component of type [T] or adds a [default] if no component was present. */
    public inline fun <reified T : GearyComponent> getOrAddPersisting(default: () -> T): T =
        get<T>() ?: addPersistingComponent(default())

    /** Gets a component of type [T] on this entity. */
    public inline fun <reified T : GearyComponent> get(): T? =
        Engine.getComponentFor(id, componentId<T>())

    /** Gets all the active components on this entity. */
    public inline fun getComponents(): Set<GearyComponent> = Engine.getComponentsFor(id)

    /** Gets all the active persisting components on this entity. */
    public inline fun getPersistingComponents(): Set<GearyComponent> =
        get<PersistingComponents>()?.persisting?.intersect(getComponents()) ?: emptySet()

    /** Gets all the active non-persisting components on this entity. */
    public inline fun getInstanceComponents(): Set<GearyComponent> =
        getComponents() - (get<PersistingComponents>()?.persisting ?: emptySet())

    /** Runs something on a component on this entity of type [T] if present. */
    public inline fun <reified T : GearyComponent> with(let: (T) -> Unit): Unit? = get<T>()?.let(let)

    /** Checks whether this entity holds a component of type [T], without regards for whether or not it's active. */
    public inline fun <reified T : GearyComponent> holds(): Boolean =
        Engine.holdsComponentFor(id, componentId<T>())

    /** Checks whether this entity has an active component of type [T] */
    public inline fun <reified T : GearyComponent> has(): Boolean = Engine.hasComponentFor(
        id,
        componentId<T>()
    )

    /** Checks whether an entity holds all of a list of [components].
     * @see holds */
    public inline fun holdsAll(components: Collection<KClass<out GearyComponent>>): Boolean =
        components.all { Engine.holdsComponentFor(id, componentId(it)) }

    /** Checks whether an entity has all of a list of [components].
     * @see has */
    public inline fun hasAll(components: Collection<KClass<out GearyComponent>>): Boolean =
        components.all { Engine.hasComponentFor(id, componentId(it)) }

    /** Enables a component of type [T] on this entity.
     * @see Engine.setFor */
    public inline fun <reified T : GearyComponent> set() {
        set(componentId<T>())
    }

    public inline fun set(component: GearyComponentId) {
        Engine.setFor(id, component)
    }

    /** Disables a component of type [T] on this entity.
     * @see Engine.unsetFor */
    public inline fun <reified T : GearyComponent> unset() {
        unset(componentId<T>())
    }

    public inline fun unset(component: GearyComponentId) {
        Engine.unsetFor(id, component)
    }

    public operator fun component1(): GearyEntityId = id
}
