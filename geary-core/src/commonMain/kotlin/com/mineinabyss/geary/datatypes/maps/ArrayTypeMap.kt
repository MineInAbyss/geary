package com.mineinabyss.geary.datatypes.maps

import com.mineinabyss.geary.datatypes.BucketedULongArray
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.engine.archetypes.Archetype


open class ArrayTypeMap : TypeMap {
    @PublishedApi
    internal val archList = arrayListOf<Archetype>()

    //    private val map: ArrayList<Record?> = arrayListOf()
//    private var archIndexes = IntArray(10)
//    private var rows = IntArray(10)
    @PublishedApi
    internal var archAndRow = BucketedULongArray()
    var size = 0

    // We don't return nullable record to avoid boxing.
    // Accessing an entity that doesn't exist is indicative of a problem elsewhere and should be made obvious.
//    override fun get(entity: Entity): Record {
//        val info = archAndRow[entity.id.toInt()]
//        return Record(
//            archList[(info shr 32).toInt()],
//            info.toInt()
//        )
//    }
    open fun getArchAndRow(entity: Entity): ULong {
        return archAndRow[entity.id.toInt()]
    }

    override fun set(entity: Entity, archetype: Archetype, row: Int) {
        val id = entity.id.toInt()
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

    override fun remove(entity: Entity) {
        val id = entity.id.toInt()
        archAndRow[id] = 0UL
    }

    override operator fun contains(entity: Entity): Boolean {
        val id = entity.id.toInt()
        return id < archAndRow.size && archAndRow[id] != 0uL
    }


    inline fun <T> runOn(entity: Entity, run: (archetype: Archetype, row: Int) -> T): T {
        val info = getArchAndRow(entity)
        return run(archList[(info shr 32).toInt()], info.toInt())
    }
}
