@file:Suppress("NOTHING_TO_INLINE")

package com.mineinabyss.geary.ecs.components

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.engine.Engine

public inline fun <T : GearyComponent> GearyEntity.addComponent(component: T): T = Engine.addComponentFor(gearyId, component)
public inline fun GearyEntity.addComponents(components: Set<GearyComponent>) {
    Engine.addComponentsFor(gearyId, components)
}

public inline fun <reified T : GearyComponent> GearyEntity.removeComponent() {
    Engine.removeComponentFor(T::class, gearyId)
}

public inline fun <reified T : GearyComponent> GearyEntity.getOrAdd(component: () -> T): T =
        get<T>() ?: addComponent(component())

public inline fun <reified T : GearyComponent> GearyEntity.get(): T? = Engine.getComponentFor(T::class, gearyId) as? T

public inline fun GearyEntity.getComponents(): Set<GearyComponent> = Engine.getComponentsFor(gearyId)

public inline fun <reified T : GearyComponent> GearyEntity.with(let: (T) -> Unit): Unit? = get<T>()?.let(let)

public inline fun <reified T : GearyComponent> GearyEntity.has(): Boolean = Engine.hasComponentFor(T::class, gearyId)
