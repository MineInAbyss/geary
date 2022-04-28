package com.mineinabyss.geary.helpers

import com.mineinabyss.geary.datatypes.GearyEntityId
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.datatypes.ENTITY_MASK

/** Gets the entity associated with this [GearyEntityId], stripping it of any roles, and runs code on it. */
public inline fun GearyEntityId.toGeary(run: GearyEntity.() -> Unit): GearyEntity = toGeary().apply(run)

/** Gets the entity associated with this [GearyEntityId], stripping it of any roles. */
public fun GearyEntityId.toGeary(): GearyEntity = GearyEntity(this and ENTITY_MASK)

/** Gets the entity associated with this [Long]. */
public fun Long.toGeary(): GearyEntity = GearyEntity(toULong() and ENTITY_MASK)
