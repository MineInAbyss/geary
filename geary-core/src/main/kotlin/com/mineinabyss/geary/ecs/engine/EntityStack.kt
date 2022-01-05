package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.GearyEntityId
import it.unimi.dsi.fastutil.longs.LongBigArrayBigList
import it.unimi.dsi.fastutil.longs.LongStack

@JvmInline
public value class EntityStack(public val stack: LongStack = LongBigArrayBigList()) {
    public fun push(id: GearyEntityId): Unit = stack.push(id.toLong())
    public fun pop(): GearyEntityId = stack.popLong().toULong()
    public fun isEmpty(): Boolean = stack.isEmpty
}
