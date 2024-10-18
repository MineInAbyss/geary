package com.mineinabyss.geary.datatypes.maps

import androidx.collection.mutableObjectListOf
import com.mineinabyss.geary.datatypes.BucketedULongArray
import com.mineinabyss.geary.datatypes.EntityId
import com.mineinabyss.geary.datatypes.EntityType
import com.mineinabyss.geary.engine.archetypes.Archetype

open class ArrayTypeMap : TypeMap {
    @PublishedApi
    internal val archList = mutableObjectListOf<Archetype>()

    @PublishedApi
    internal var archAndRow = BucketedULongArray()
    var size = 0

    // We don't return nullable record to avoid boxing.
    // Accessing an entity that doesn't exist is indicative of a problem elsewhere and should be made obvious.
    open fun getArchAndRow(entity: EntityId): ULong {
        return archAndRow[entity.toInt()]
    }

    override fun set(entity: EntityId, archetype: Archetype, row: Int) {
        val id = entity.toInt()
        archAndRow[id] = (indexOrAdd(archetype).toULong() shl 32) or row.toULong()
    }

    fun indexOrAdd(archetype: Archetype): Int {
        if (archetype.indexInRecords != -1) return archetype.indexInRecords
        val index = archList.indexOf(archetype)
        archetype.indexInRecords = index
        return if (index == -1) {
            archList.add(archetype)
            archList.lastIndex
        } else index
    }

    override fun remove(entity: EntityId) {
        val id = entity.toInt()
        archAndRow[id] = 0UL
    }

    override operator fun contains(entity: EntityId): Boolean {
        val id = entity.toInt()
        return id < archAndRow.size && archAndRow[id] != 0uL
    }


    inline fun <T> runOn(entity: EntityId, run: (archetype: Archetype, row: Int) -> T): T {
        val info = getArchAndRow(entity)
        return run(archList[(info shr 32).toInt()], info.toInt())
    }

    fun getType(entity: EntityId): EntityType = runOn(entity) { archetype, _ ->
        archetype.type
    }
}
