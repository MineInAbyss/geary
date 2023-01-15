package com.mineinabyss.geary.datatypes

import com.mineinabyss.geary.helpers.toGeary
import com.soywiz.kds.DoubleQueue
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

class EntityStack(private val stack: DoubleQueue = DoubleQueue()) {
    private val removedEntitiesLock = SynchronizedObject()
    fun push(entity: Entity) {
        synchronized(removedEntitiesLock) {
            stack.enqueue(Double.fromBits(entity.id.toLong()))
        }
    }

    fun pop(): Entity = synchronized(removedEntitiesLock) {
        stack.dequeue().toRawBits().toGeary()
    }
}
