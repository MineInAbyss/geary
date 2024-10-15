package com.mineinabyss.geary.datatypes

import com.mineinabyss.geary.modules.Geary
import kotlin.jvm.JvmInline

typealias EntityIdArray = ULongArray

fun EntityIdArray.toEntityArray(world: Geary): EntityArray {
    return EntityArray(world, this)
}

class EntityArray(
    val world: Geary,
    val ids: EntityIdArray,
): Collection<Entity> {
    override val size: Int get() = ids.size

    override fun isEmpty(): Boolean = ids.isEmpty()

    override fun iterator(): Iterator<Entity> = object : Iterator<Entity> {
        private var index = 0
        override fun hasNext(): Boolean = index < ids.size
        override fun next(): Entity = GearyEntity(ids[index++], world)
    }

    override fun containsAll(elements: Collection<Entity>): Boolean {
        return elements.all { contains(it) }
    }

    override fun contains(element: Entity): Boolean {
        return ids.contains(element.id)
    }

    inline fun fastForEach(action: (Entity) -> Unit) {
        for (i in ids.indices) action(GearyEntity(ids[i], world))
    }
}
