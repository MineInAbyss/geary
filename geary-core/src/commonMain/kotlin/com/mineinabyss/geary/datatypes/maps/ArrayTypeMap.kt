package com.mineinabyss.geary.datatypes.maps

import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.Record
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.modules.archetypes


class ArrayTypeMap : TypeMap {
    @PublishedApi
    internal val archList = arrayListOf<Archetype>()
    //    private val map: ArrayList<Record?> = arrayListOf()
//    private var archIndexes = IntArray(10)
//    private var rows = IntArray(10)
    @PublishedApi
    internal var archAndRow = ULongArray(10)
    var size = 0

    // We don't return nullable record to avoid boxing.
    // Accessing an entity that doesn't exist is indicative of a problem elsewhere and should be made obvious.
    override fun get(entity: Entity): Record {
        val info = archAndRow[entity.id.toInt()]
        return Record(
            archList[(info shr 32).toInt()],
            info.toInt()
        )
    }

    override fun set(entity: Entity, archetype: Archetype, row: Int) {
        val id = entity.id.toInt()
        if (id > size) {
            size = id
            while (id >= archAndRow.size)
                grow()
        }

        archAndRow[id] = (indexOrAdd(archetype).toULong() shl 32 )or row.toULong()
//        archIndexes[id] = indexOrAdd(archetype)
    }

    fun indexOrAdd(archetype: Archetype): Int {
        val index = archList.indexOf(archetype)
        return if (index == -1) {
            archList.add(archetype)
            archList.lastIndex
        } else index
    }

    fun grow() {
        archAndRow = archAndRow.copyOf(size * 2)
//        rows = rows.copyOf(size * 2)
//        archIndexes = archIndexes.copyOf(size * 2)
    }


    override fun remove(entity: Entity) {
        val id = entity.id.toInt()
        archAndRow[id] = 0UL
//        archIndexes[id] = -1
//        rows[id] = -1
    }

    override operator fun contains(entity: Entity): Boolean {
        val id = entity.id.toInt()
        return id < archAndRow.size && archAndRow[id] != 0uL
    }


    inline fun runOn(entity: Entity, run: (Archetype, Int) -> Unit) {
        val info = archAndRow[entity.id.toInt()]
        run(archList[(info shr 32).toInt()], info.toInt())
    }
}
