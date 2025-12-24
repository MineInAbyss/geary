package com.mineinabyss.geary.systems.accessors.type

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.systems.accessors.ReadOnlyAccessor

class ComponentOrDefaultAccessor<T>(
    val id: ComponentId,
    val default: () -> T,
) : ReadOnlyAccessor<T> {
    private var cachedIndex = -1
    private var cachedDataArray: Array<T> = arrayOf<Any>() as Array<T>
    private var defaultForArchetype: T? = null

    override fun load(archetype: Archetype) {
        cachedIndex = archetype.indexOf(id)
        if (cachedIndex != -1) {
            defaultForArchetype = default()
            cachedDataArray = archetype.componentData[cachedIndex].content as Array<T>
        }
    }

    @OptIn(UnsafeAccessors::class)
    override fun get(row: Int): T {
        if (cachedIndex == -1) return defaultForArchetype!!
        else return cachedDataArray[row]
    }
}
