package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.toGeary
import it.unimi.dsi.fastutil.longs.LongBigArrayBigList
import it.unimi.dsi.fastutil.longs.LongStack

@JvmInline
public value class EntityStack(public val stack: LongStack = LongBigArrayBigList()) {
    public fun push(entity: GearyEntity): Unit = stack.push(entity.id.toLong())
    public fun pop(): GearyEntity = stack.popLong().toGeary()
    public fun isEmpty(): Boolean = stack.isEmpty
}
