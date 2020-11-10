@file:Suppress("NOTHING_TO_INLINE")

package com.mineinabyss.geary.ecs.components

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.engine.Engine

inline fun <T : GearyComponent> GearyEntity.addComponent(component: T): T = Engine.addComponentFor(gearyId, component)
inline fun GearyEntity.addComponents(components: Set<GearyComponent>) = Engine.addComponentsFor(gearyId, components)

inline fun <reified T : GearyComponent> GearyEntity.removeComponent() =
        Engine.removeComponentFor(T::class, gearyId)

inline fun <reified T : GearyComponent> GearyEntity.getOrAdd(component: () -> T) = get<T>() ?: addComponent(component())

inline fun <reified T : GearyComponent> GearyEntity.get(): T? = Engine.getComponentFor(T::class, gearyId) as? T

inline fun GearyEntity.getComponents(): Set<GearyComponent> = Engine.getComponentsFor(gearyId)

inline fun <reified T : GearyComponent> GearyEntity.with(let: (T) -> Unit) = get<T>()?.let(let)

inline fun <reified T : GearyComponent> GearyEntity.has() = Engine.hasComponentFor(T::class, gearyId)