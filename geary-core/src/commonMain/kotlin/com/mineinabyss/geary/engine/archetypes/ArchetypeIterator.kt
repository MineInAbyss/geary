package com.mineinabyss.geary.engine.archetypes

import com.mineinabyss.geary.systems.accessors.TargetScope
import com.mineinabyss.geary.systems.accessors.AccessorHolder
import com.mineinabyss.geary.systems.accessors.RawAccessorDataScope

public data class ArchetypeIterator(
    public val archetype: Archetype,
    public val holder: AccessorHolder,
) {
    private val perArchCache = holder.cacheForArchetype(archetype)
    private var row: Int = 0

    internal inline fun forEach(upTo: Int, crossinline run: (TargetScope) -> Unit) {
        while (row < archetype.size && row <= upTo) {
            val dataScope =
                RawAccessorDataScope(
                    archetype = archetype,
                    row = row++,
                    perArchetypeData = perArchCache
                )
            holder.forEachCombination(dataScope) { data ->
                run(
                    TargetScope(
                        entity = dataScope.entity,
                        data = data
                    )
                )
            }
        }
        //FIXME clean up removed components
    }
}
