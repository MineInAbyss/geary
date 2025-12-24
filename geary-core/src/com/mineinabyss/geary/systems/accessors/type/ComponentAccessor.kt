package com.mineinabyss.geary.systems.accessors.type

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.engine.ComponentProvider
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.systems.accessors.FamilyMatching
import com.mineinabyss.geary.systems.accessors.ReadWriteAccessor

@OptIn(UnsafeAccessors::class)
class ComponentAccessor<T : Any>(
    val id: ComponentId,
) : ReadWriteAccessor<T>, FamilyMatching {
    override val family = family { hasSet(id) }
    private var cachedDataArray: Array<T> = arrayOf<Any>() as Array<T>

    override fun load(archetype: Archetype) {
        val cachedIndex = archetype.indexOf(id)
        if (cachedIndex != -1) {
            cachedDataArray = archetype.componentData[cachedIndex].content as Array<T>
        }
    }

    override fun get(row: Int): T {
        return cachedDataArray[row]
    }

    override fun set(row: Int, value: T) {
        cachedDataArray[row] = value
    }
}
