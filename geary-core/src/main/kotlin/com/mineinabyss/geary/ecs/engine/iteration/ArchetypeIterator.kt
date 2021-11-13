package com.mineinabyss.geary.ecs.engine.iteration

import com.mineinabyss.geary.ecs.api.entities.toGeary
import com.mineinabyss.geary.ecs.engine.Archetype
import com.mineinabyss.geary.ecs.engine.iteration.accessors.AccessorDataScope
import com.mineinabyss.geary.ecs.engine.iteration.accessors.AccessorHolder
import com.mineinabyss.geary.ecs.engine.iteration.accessors.QueryResult
import com.mineinabyss.geary.ecs.query.accessors.Accessor

public data class ArchetypeIterator(
    public val archetype: Archetype,
    public val holder: AccessorHolder,
) : Iterator<QueryResult> {
    private val accessors: List<Accessor<*>> = holder.accessors

    init {
        accessors.forEachIndexed { i, accessor ->
            for (cached in accessor.cached) {
                cachedValues[i].add(with(cached) { calculate() })
            }
        }
    }

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

    override fun next(): QueryResult {
        if (combinationsIterator?.hasNext() != true) {
            val destinationRow = movedRows.firstOrNull() ?: row++
            movedRows.remove(destinationRow)
            val entity = archetype.ids[destinationRow].toGeary()

            combinationsIterator = holder.iteratorFor(
                AccessorDataScope(
                    archetype = archetype,
                    row = destinationRow,
                    entity = entity,
                )
            )
        }

        return QueryResult(
            entity = combinationsIterator!!.dataScope.entity,
            data = combinationsIterator!!.next(),
        )
    }
}
