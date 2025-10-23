package com.mineinabyss.geary.datatypes

import androidx.collection.MutableLongList
import com.mineinabyss.geary.modules.Geary

typealias EntityIdArray = ULongArray

fun EntityIdArray.toEntityArray(world: Geary): EntityArray {
    return EntityArray(world, this)
}

fun MutableLongList.toEntityArray(world: Geary): EntityArray {
    return EntityArray(world, ULongArray(size) { get(it).toULong() })
}

/**
 * An array of [EntityId]s with an associated [world].
 * Avoids string boxed [GearyEntity] instances for each entity, with helpers to completely avoid boxing like [forEachId].
 */
class EntityArray(
    val world: Geary,
    val ids: EntityIdArray,
) : Collection<Entity> {
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

    inline fun forEachId(action: (EntityId) -> Unit) {
        for (i in ids.indices) action(ids[i])
    }

    inline fun flatMap(transform: (Entity) -> EntityArray): EntityArray {
        return ids.flatMapTo(arrayListOf()) { transform(Entity(it, world)).ids }.toULongArray().toEntityArray(world)
    }

    operator fun minus(other: Collection<Entity>): EntityArray {
        return ids.minus(other.map { it.id }.toSet()).toULongArray().toEntityArray(world)
    }
}
