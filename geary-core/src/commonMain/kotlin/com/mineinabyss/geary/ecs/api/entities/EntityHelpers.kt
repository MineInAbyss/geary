package com.mineinabyss.geary.ecs.api.entities

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyEntityId
import com.mineinabyss.geary.ecs.engine.ENTITY_MASK

/** Gets the entity associated with this [GearyEntityId], stripping it of any roles, and runs code on it. */
public inline fun GearyEntityId.toGeary(run: GearyEntity.() -> Unit): GearyEntity = toGeary().apply(run)

/** Gets the entity associated with this [GearyEntityId], stripping it of any roles. */
public fun GearyEntityId.toGeary(): GearyEntity = GearyEntity(this and ENTITY_MASK)

/** Gets the entity associated with this [Long]. */
public fun Long.toGeary(): GearyEntity = GearyEntity(toULong() and ENTITY_MASK)

/**
 * Swaps components of type [T] on two entities.
 *
 * @return Whether at least one component of type [T] was present and swapped places.
 */
public inline fun <reified T : GearyComponent> GearyEntity?.swapComponent(with: GearyEntity?): Boolean {
    val component = this?.get<T>()
    val otherComponent = with?.get<T>()

    if (component != null)
        with?.set(component)
    else
        with?.remove<T>()

    if (otherComponent != null)
        this?.set(otherComponent)
    else
        this?.remove<T>()

    return component != null || otherComponent != null
}
