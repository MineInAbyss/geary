package com.mineinabyss.geary.datatypes

import com.mineinabyss.geary.helpers.toGeary

class EntityStack(
    @PublishedApi
    internal val stack: ArrayDeque<Long> = ArrayDeque()
) {
    fun push(entity: Entity) {
        stack.add(entity.id.toLong())
    }

    inline fun popOrElse(orElse: () -> Entity): Entity =
        if (stack.isEmpty()) orElse()
        else stack.removeFirst().toGeary()
}
