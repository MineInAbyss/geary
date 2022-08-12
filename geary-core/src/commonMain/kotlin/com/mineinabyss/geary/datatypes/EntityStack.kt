package com.mineinabyss.geary.datatypes

import com.mineinabyss.geary.helpers.toGeary
import com.soywiz.kds.DoubleQueue
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

public class EntityStack(private val stack: DoubleQueue = DoubleQueue()) {
    private val removedEntitiesLock = SynchronizedObject()
    public fun push(entity: Entity) {
        synchronized(removedEntitiesLock) {
            stack.enqueue(Double.fromBits(entity.id.toLong()))
        }
    }

    public fun pop(): Entity = synchronized(removedEntitiesLock) {
        stack.dequeue().toRawBits().toGeary()
    }
}
