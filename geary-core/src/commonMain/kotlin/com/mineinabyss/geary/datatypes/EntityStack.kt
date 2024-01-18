package com.mineinabyss.geary.datatypes

import com.mineinabyss.geary.helpers.toGeary

class EntityStack(private val stack: ArrayDeque<Long> = ArrayDeque()) {
    fun push(entity: Entity) {
        stack.add(entity.id.toLong())
    }

    fun pop(): Entity? =
        if (stack.isEmpty()) null
        else stack.removeFirst().toGeary()
}
