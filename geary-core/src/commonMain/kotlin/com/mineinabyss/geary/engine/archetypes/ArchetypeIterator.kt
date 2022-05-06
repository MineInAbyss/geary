package com.mineinabyss.geary.engine.archetypes

import com.mineinabyss.geary.systems.accessors.AccessorHolder
import com.mineinabyss.geary.systems.accessors.RawAccessorDataScope
import com.mineinabyss.geary.systems.accessors.TargetScope

@PublishedApi
internal data class ArchetypeIterator(
    val archetype: Archetype,
    val holder: AccessorHolder,
) {
    val perArchCache = holder.cacheForArchetype(archetype)
    var row: Int = 0

    inline fun forEach(upTo: Int, crossinline run: (TargetScope) -> Unit) {
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
