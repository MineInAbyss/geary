package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.toGeary
import com.soywiz.kds.DoubleQueue
import kotlin.jvm.JvmInline

@JvmInline
public value class EntityStack(private val stack: DoubleQueue = DoubleQueue()) {
    // TODO benchmark this sort of conversion vs boxing
    public fun push(entity: GearyEntity) {
        stack.enqueue(Double.fromBits(entity.id.toLong()))
    }

    public fun pop(): GearyEntity = stack.dequeue().toRawBits().toGeary()
}
