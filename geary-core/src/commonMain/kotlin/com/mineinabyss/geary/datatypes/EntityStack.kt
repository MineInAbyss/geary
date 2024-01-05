package com.mineinabyss.geary.datatypes

import com.mineinabyss.geary.helpers.toGeary
import korlibs.datastructure.DoubleQueue

class EntityStack(private val stack: DoubleQueue = DoubleQueue()) {
    fun push(entity: Entity) {
        stack.enqueue(Double.fromBits(entity.id.toLong()))
    }

    fun pop(): Entity? =
        if (stack.isEmpty()) null
        else stack.dequeue().toRawBits().toGeary()
}
