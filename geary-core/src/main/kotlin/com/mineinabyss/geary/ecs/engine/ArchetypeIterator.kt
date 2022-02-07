package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.accessors.AccessorHolder
import com.mineinabyss.geary.ecs.accessors.RawAccessorDataScope
import com.mineinabyss.geary.ecs.accessors.TargetScope
import it.unimi.dsi.fastutil.ints.IntOpenHashSet

public data class ArchetypeIterator(
    public val archetype: Archetype,
    public val holder: AccessorHolder,
) {
    private val perArchCache = holder.cacheForArchetype(archetype)
    private var row: Int = 0

    /** Set of elements moved during a component removal. Represents the resulting row to original row. */
    private val movedRows: IntOpenHashSet = IntOpenHashSet()

    internal fun addMovedRow(originalRow: Int, resultingRow: Int) {
        if (resultingRow > row) return
        movedRows.remove(originalRow)
        movedRows.add(resultingRow)
    }

    internal inline fun forEach(crossinline run: (TargetScope) -> Unit) {
        while (row < archetype.size || movedRows.isNotEmpty()) {
            val destinationRow = movedRows.firstOrNull().also {
                if (it != null) movedRows.remove(it)
            } ?: row++
            val dataScope =
                RawAccessorDataScope(
                    archetype = archetype,
                    row = destinationRow,
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
