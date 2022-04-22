package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.toGeary
import com.soywiz.kds.DoubleQueue
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlin.jvm.JvmInline

public class EntityStack(private val stack: DoubleQueue = DoubleQueue()) {
    private val removedEntitiesLock = SynchronizedObject()
    public fun push(entity: GearyEntity) {
        synchronized(removedEntitiesLock) {
            stack.enqueue(Double.fromBits(entity.id.toLong()))
        }
    }

    public fun pop(): GearyEntity = synchronized(removedEntitiesLock) {
        stack.dequeue().toRawBits().toGeary()
    }
}
