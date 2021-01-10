@file:Suppress("NOTHING_TO_INLINE")

package com.mineinabyss.geary.ecs.components

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.engine.Engine
import kotlin.reflect.KClass

/** Adds a component of type [T] to this entity */
public inline fun <reified T : GearyComponent> GearyEntity.addComponent(component: T): T =
    Engine.addComponentFor(T::class, gearyId, component)

/** Adds a list of [components] to this entity */
public inline fun GearyEntity.addComponents(components: Set<GearyComponent>) {
    Engine.addComponentsFor(gearyId, components)
}

/**
 * Adds a persisting [component] to this entity, which will be serialized in some way if possible.
 *
 * Ex. for bukkit entities this is done through a [PersistentDataContainer].
 */
public inline fun <reified T : GearyComponent> GearyEntity.addPersistingComponent(component: T): T {
    addComponent(component)
    getOrAdd { PersistingComponents() }.add(component)
    return component
}

/**
 * Adds a list of persisting [components]
 * @see addPersistingComponent
 */
public fun GearyEntity.addPersistingComponents(components: Set<GearyComponent>) {
    if (components.isEmpty()) return //avoid adding a persisting components component if there are none to add
    addComponents(components)
    getOrAdd { PersistingComponents() }.addAll(components)
}

/**
 * Removes a component of type [T] from this entity.
 *
 * @return Whether the component was present before removal.
 */
public inline fun <reified T : GearyComponent> GearyEntity.removeComponent(): Boolean =
    Engine.removeComponentFor(T::class, gearyId)

/** Gets a component of type [T] or adds a [default] if no component was present. */
public inline fun <reified T : GearyComponent> GearyEntity.getOrAdd(default: () -> T): T =
    get<T>() ?: addComponent(default())

/** Gets a persisting component of type [T] or adds a [default] if no component was present. */
public inline fun <reified T : GearyComponent> GearyEntity.getOrAddPersisting(default: () -> T): T =
    get<T>() ?: addPersistingComponent(default())

/** Gets a component of type [T] on this entity. */
public inline fun <reified T : GearyComponent> GearyEntity.get(): T? = Engine.getComponentFor(T::class, gearyId)

/** Gets all the active components on this entity. */
public inline fun GearyEntity.getComponents(): Set<GearyComponent> = Engine.getComponentsFor(gearyId)

/** Gets all the active persisting components on this entity. */
public inline fun GearyEntity.getPersistingComponents(): Set<GearyComponent> =
    get<PersistingComponents>()?.persisting?.intersect(getComponents()) ?: emptySet()

/** Gets all the active non-persisting components on this entity. */
public inline fun GearyEntity.getInstanceComponents(): Set<GearyComponent> =
    getComponents() - (get<PersistingComponents>()?.persisting ?: emptySet())

/** Runs something on a component on this entity of type [T] if present. */
public inline fun <reified T : GearyComponent> GearyEntity.with(let: (T) -> Unit): Unit? = get<T>()?.let(let)

/** Checks whether this entity holds a component of type [T], without regards for whether or not it's active. */
public inline fun <reified T : GearyComponent> GearyEntity.holds(): Boolean =
    Engine.holdsComponentFor(T::class, gearyId)

/** Checks whether this entity has an active component of type [T] */
public inline fun <reified T : GearyComponent> GearyEntity.has(): Boolean = Engine.hasComponentFor(T::class, gearyId)

/** Checks whether an entity holds all of a list of [components].
 * @see holds */
public inline fun GearyEntity.holdsAll(components: Collection<KClass<out GearyComponent>>): Boolean =
    components.all { Engine.holdsComponentFor(it, gearyId) }

/** Checks whether an entity has all of a list of [components].
 * @see has */
public inline fun GearyEntity.hasAll(components: Collection<KClass<out GearyComponent>>): Boolean =
    components.all { Engine.hasComponentFor(it, gearyId) }

/** Enables a component of type [T] on this entity.
 * @see Engine.enableComponentFor */
public inline fun <reified T : GearyComponent> GearyEntity.enable() {
    Engine.enableComponentFor(T::class, gearyId)
}

/** Disables a component of type [T] on this entity.
 * @see Engine.disableComponentFor */
public inline fun <reified T : GearyComponent> GearyEntity.disable() {
    Engine.disableComponentFor(T::class, gearyId)
}

/**
 * Swaps components of type [T] on two entities.
 *
 * @return Whether or not at least one component of type [T] was present and swapped places.
 */
public inline fun <reified T : GearyComponent> GearyEntity?.swapComponent(with: GearyEntity?): Boolean {
    val component = this?.get<T>()
    val otherComponent = with?.get<T>()

    if (component != null)
        with?.addComponent(component)
    else
        with?.removeComponent<T>()

    if (otherComponent != null)
        this?.addComponent(otherComponent)
    else
        this?.removeComponent<T>()

    return component != null || otherComponent != null
}
