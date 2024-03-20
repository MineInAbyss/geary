package com.mineinabyss.geary.datatypes.maps

import com.mineinabyss.geary.datatypes.GearyComponentId
import com.mineinabyss.geary.datatypes.IdList
import com.mineinabyss.geary.engine.archetypes.Archetype

/**
 * Inlined class that acts as a map of components to archetypes. Uses archetype ids for better performance.
 */

class CompId2ArchetypeMap {
    //    val inner = Long2ObjectArrayMap<Archetype>()
    val ids = IdList()
    val values = mutableListOf<Archetype>()
    //    actual operator fun get(id: GearyComponentId): Archetype? =
//        values[entries.indexOf(id).also { if (it == -1) return null }]
    operator fun set(id: GearyComponentId, archetype: Archetype) {
        val index = ids.indexOf(id)
        if (index == -1) {
            ids.add(id)
            values.add(archetype)
        } else {
            values[index] = archetype
        }
    }

    fun remove(id: GearyComponentId) {
        val index = ids.indexOf(id)
        if (index != -1) {
            ids.removeAt(index)
            values[index] = values[values.lastIndex]
            values.removeLast()
        }
    }

    fun clear() {
        ids.size = 0
        values.clear()
    }

    inline fun forEach(action: (ULong, Archetype) -> Unit) {
        for(i in 0 until ids.size) {
            action(ids[i], values[i])
        }
    }

    val size: Int get() = ids.size

    operator fun contains(id: GearyComponentId): Boolean = ids.indexOf(id) != -1

    inline fun getOrElse(id: GearyComponentId, defaultValue: () -> Archetype): Archetype {
        val index = ids.indexOf(id)
        return if (index == -1) defaultValue() else values[index]
    }

    inline fun getOrSet(id: GearyComponentId, put: () -> Archetype): Archetype {
        val index = ids.indexOf(id)
        if (index == -1) {
            val arc = put()
            ids.add(id)
            values.add(arc)
            return arc
        }
        return values[index]
    }
}
