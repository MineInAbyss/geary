package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.toGeary
import java.util.concurrent.ConcurrentLinkedQueue

@JvmInline
public value class EntityStack(public val stack: ConcurrentLinkedQueue<Long> = ConcurrentLinkedQueue()) {
    public fun push(entity: GearyEntity): Boolean = stack.add(entity.id.toLong())
    public fun pop(): GearyEntity = stack.remove().toGeary()
}
