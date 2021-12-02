package com.mineinabyss.geary.ecs.api.entities

import com.mineinabyss.geary.ecs.api.GearyEntityId
import com.mineinabyss.geary.ecs.engine.ENTITY_MASK

/** Gets the entity associated with [this@toGeary]. */
//TODO think a bit more about the benefits of automatically adding a mask vs possible bugs with internal code
// working with relations accidentally using this
@Suppress("NOTHING_TO_INLINE")
public inline fun GearyEntityId.toGeary(): GearyEntity = GearyEntity(this and ENTITY_MASK)

@Suppress("NOTHING_TO_INLINE")
public inline fun GearyEntityId.toGearyNoMask(): GearyEntity = GearyEntity(this)

/** Gets the entity associated with [this@toGeary]. */
@Suppress("NOTHING_TO_INLINE")
public inline fun Long.toGeary(): GearyEntity = GearyEntity(toULong() and ENTITY_MASK)