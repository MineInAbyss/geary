package com.mineinabyss.geary.datatypes.maps

import com.mineinabyss.geary.datatypes.EntityType
import com.mineinabyss.geary.datatypes.GearyComponentId
import com.mineinabyss.geary.engine.archetypes.Archetype

/**
 * Inlined class that acts as a map of components to archetypes. Uses archetype ids for better performance.
 */
class CompId2ArchetypeMap {
    val inner: MutableMap<ULong, Archetype> = mutableMapOf()
    operator fun get(id: GearyComponentId): Archetype? = inner[id]
    operator fun set(id: GearyComponentId, archetype: Archetype) {
        inner[id] = archetype
    }

    operator fun contains(id: GearyComponentId): Boolean = inner.containsKey(id)
}


class CompId2ArchetypePackedArrayMap {
    val packedArray = mutableListOf<Archetype>()
    var type = EntityType()
    val inner: MutableMap<ULong, Archetype> = mutableMapOf()
    operator fun get(id: GearyComponentId): Archetype? {
        val index = type.indexOf(id)
        if (index == -1) return null
        return packedArray[index]
    }//inner[id]

    operator fun set(id: GearyComponentId, archetype: Archetype) {
//        inner[id] = archetype

        type += id
        packedArray.add(type.indexOf(id), archetype)
    }

    operator fun contains(id: GearyComponentId): Boolean = type.contains(id)

//    inline fun getOrPut(id: GearyComponentId, default: () -> Archetype): Archetype {
//        return inner.getOrPut(id, default)
//    }
}
