package com.mineinabyss.geary.ecs.api.entities

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.GearyEntityId

/** Gets the entity associated with [id] and runs code on it. */
public inline fun geary(id: GearyEntityId, run: GearyEntity.() -> Unit): GearyEntity =
    geary(id).apply(run)

/** Gets the entity associated with [id]. */
@Suppress("NOTHING_TO_INLINE")
public inline fun geary(id: GearyEntityId): GearyEntity = GearyEntity(id)

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
