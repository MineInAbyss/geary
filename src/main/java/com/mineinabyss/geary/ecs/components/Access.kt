@file:Suppress("NOTHING_TO_INLINE")

package com.mineinabyss.geary.ecs.components

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.engine.Engine
import kotlin.reflect.KClass

public inline fun <reified T : GearyComponent> GearyEntity.addComponent(component: T): T =
        Engine.addComponentFor(T::class, gearyId, component)

public inline fun GearyEntity.addComponents(components: Set<GearyComponent>) {
    Engine.addComponentsFor(gearyId, components)
}

public inline fun <reified T : GearyComponent> GearyEntity.addPersistingComponent(component: T): T {
    addComponent(component)
    getOrAdd { PersistingComponents() }.add(component)
    return component
}

public fun GearyEntity.addPersistingComponents(components: Set<GearyComponent>) {
    if (components.isEmpty()) return //avoid adding a persisting components component if there are none to add
    addComponents(components)
    getOrAdd { PersistingComponents() }.addAll(components)
}

public inline fun <reified T : GearyComponent> GearyEntity.removeComponent(): Boolean =
        Engine.removeComponentFor(T::class, gearyId)

public inline fun <reified T : GearyComponent> GearyEntity.getOrAdd(component: () -> T): T =
        get<T>() ?: addComponent(component())

public inline fun <reified T : GearyComponent> GearyEntity.getOrAddPersisting(component: () -> T): T =
        get<T>() ?: addPersistingComponent(component())

public inline fun <reified T : GearyComponent> GearyEntity.get(): T? = Engine.getComponentFor(T::class, gearyId)

public inline fun GearyEntity.getComponents(): Set<GearyComponent> = Engine.getComponentsFor(gearyId)

public inline fun GearyEntity.getPersistingComponents(): Set<GearyComponent> =
        get<PersistingComponents>()?.persisting?.intersect(getComponents()) ?: emptySet()

public inline fun GearyEntity.getInstanceComponents(): Set<GearyComponent> =
        getComponents() - (get<PersistingComponents>()?.persisting ?: emptySet())

public inline fun <reified T : GearyComponent> GearyEntity.with(let: (T) -> Unit): Unit? = get<T>()?.let(let)

public inline fun <reified T : GearyComponent> GearyEntity.holds(): Boolean = Engine.holdsComponentFor(T::class, gearyId)

public inline fun <reified T : GearyComponent> GearyEntity.has(): Boolean = Engine.hasComponentFor(T::class, gearyId)

public inline fun GearyEntity.hasAll(components: Collection<KClass<out GearyComponent>>): Boolean =
        components.all { Engine.hasComponentFor(it, gearyId) }

public inline fun <reified T : GearyComponent> GearyEntity.enable() {
    Engine.enableComponentFor(T::class, gearyId)
}

public inline fun <reified T : GearyComponent> GearyEntity.disable() {
    Engine.disableComponentFor(T::class, gearyId)
}

/**
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
