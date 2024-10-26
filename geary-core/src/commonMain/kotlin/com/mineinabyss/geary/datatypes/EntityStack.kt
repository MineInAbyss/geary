package com.mineinabyss.geary.datatypes

class EntityStack(
    @PublishedApi
    internal val stack: ArrayDeque<Long> = ArrayDeque(),
) {
    fun push(entity: EntityId) {
        stack.add(entity.toLong())
    }

    inline fun popOrElse(orElse: () -> EntityId): EntityId =
        if (stack.isEmpty()) orElse()
        else stack.removeFirst().toULong()
}
