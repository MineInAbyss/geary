package com.mineinabyss.geary.systems.accessors.type

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.engine.ComponentProvider
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.systems.accessors.Accessor
import com.mineinabyss.geary.systems.accessors.FamilyMatching
import com.mineinabyss.geary.systems.accessors.ReadWriteAccessor
import com.mineinabyss.geary.systems.query.Query

@OptIn(UnsafeAccessors::class)
class ComponentAccessor<T : Any>(
    comp: ComponentProvider,
    override val originalAccessor: Accessor?,
    val id: ComponentId,
) : ReadWriteAccessor<T>, FamilyMatching {
    override val family = family { hasSet(id) }

    private var cachedIndex = -1
    private var cachedDataArray: Array<T> = arrayOf<Any>() as Array<T>

    fun updateCache(archetype: Archetype) {
        cachedIndex = archetype.indexOf(id)
        @Suppress("UNCHECKED_CAST")
        if (cachedIndex != -1) cachedDataArray =
            archetype.componentData[cachedIndex].content as Array<T>
    }

    override fun get(query: Query): T {
        return cachedDataArray[query.row]
    }

    override fun set(query: Query, value: T) {
        cachedDataArray[query.row] = value
    }
}
