package com.mineinabyss.geary.helpers

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.ENTITY_MASK
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.EntityId
import com.mineinabyss.geary.modules.Geary

/** Gets the entity associated with this [EntityId], stripping it of any roles, and runs code on it. */
inline fun EntityId.toGeary(world: Geary, run: Entity.() -> Unit): Entity = toGeary(world).apply(run)

/** Gets the entity associated with this [EntityId], stripping it of any roles. */
fun EntityId.toGeary(world: Geary): Entity = Entity(this and ENTITY_MASK, world)

/** Gets the entity associated with this [Long]. */
fun Long.toGeary(world: Geary): Entity = Entity(toULong() and ENTITY_MASK, world)

val Geary.NO_ENTITY: Entity get() = 0L.toGeary(this)

const val NO_COMPONENT: ComponentId = 0uL
