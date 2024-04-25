package com.mineinabyss.geary.helpers

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.ENTITY_MASK
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.EntityId

/** Gets the entity associated with this [EntityId], stripping it of any roles, and runs code on it. */
inline fun EntityId.toGeary(run: Entity.() -> Unit): Entity = toGeary().apply(run)

/** Gets the entity associated with this [EntityId], stripping it of any roles. */
fun EntityId.toGeary(): Entity = Entity(this and ENTITY_MASK)

/** Gets the entity associated with this [Long]. */
fun Long.toGeary(): Entity = Entity(toULong() and ENTITY_MASK)

val NO_ENTITY: Entity = 0L.toGeary()

const val NO_COMPONENT: ComponentId = 0uL
