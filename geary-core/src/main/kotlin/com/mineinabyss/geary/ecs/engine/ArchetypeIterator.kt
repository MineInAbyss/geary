package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.accessors.AccessorHolder
import com.mineinabyss.geary.ecs.accessors.TargetScope
import com.mineinabyss.geary.ecs.accessors.RawAccessorDataScope
import com.mineinabyss.geary.ecs.api.entities.toGeary

public data class ArchetypeIterator(
    public val archetype: Archetype,
    public val holder: AccessorHolder,
) : Iterator<TargetScope> {
    private val perArchCache = holder.cacheForArchetype(archetype)
    private var row: Int = 0

    override fun hasNext(): Boolean =
        (row < archetype.size || movedRows.isNotEmpty() || combinationsIterator?.hasNext() == true)
            .also { if (!it) archetype.finalizeIterator(this) }

    /** Set of elements moved during a component removal. Represents the resulting row to original row. */
    private val movedRows = mutableSetOf<Int>()

    internal fun addMovedRow(originalRow: Int, resultingRow: Int) {
        if (resultingRow > row) return
        movedRows.remove(originalRow)
        movedRows.add(resultingRow)
    }


    private var combinationsIterator: AccessorHolder.AccessorCombinationsIterator? = null

    override fun next(): TargetScope {
        if (combinationsIterator?.hasNext() != true) {
            val destinationRow = movedRows.firstOrNull() ?: row++
            movedRows.remove(destinationRow)
            val entity = archetype.ids.getLong( destinationRow).toGeary()

            combinationsIterator = holder.iteratorFor(
                RawAccessorDataScope(
                    archetype = archetype,
                    row = destinationRow,
                    entity = entity,
                    perArchetypeData = perArchCache
                )
            )
        }

        return TargetScope(
            entity = combinationsIterator!!.dataScope.entity,
            data = combinationsIterator!!.next()
        )
    }
}
