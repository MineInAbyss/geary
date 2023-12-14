package com.mineinabyss.geary.datatypes.maps

import com.mineinabyss.geary.datatypes.GearyComponentId
import com.mineinabyss.geary.engine.archetypes.Archetype
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap

actual class CompId2ArchetypeMap {
    val inner = Long2ObjectArrayMap<Archetype>()
    actual operator fun get(id: GearyComponentId): Archetype? = inner[id.toLong()]
    actual operator fun set(id: GearyComponentId, archetype: Archetype) {
        inner[id.toLong()] = archetype
    }

    actual fun remove(id: GearyComponentId) {
        inner.remove(id.toLong())
    }

    actual fun entries(): Set<Map.Entry<ULong, Archetype>> = inner.mapKeys { it.key.toULong() }.entries

    actual val size: Int get() = inner.size

    actual operator fun contains(id: GearyComponentId): Boolean = inner.containsKey(id.toLong())

    actual inline fun getOrSet(id: GearyComponentId, put: () -> Archetype): Archetype {
        return inner[id.toLong()] ?: put().also { set(id, it) }
    }
}
