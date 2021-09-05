package com.mineinabyss.geary.ecs.engine.iteration

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.toGeary
import com.mineinabyss.geary.ecs.engine.Archetype
import com.mineinabyss.geary.ecs.query.Query
import com.mineinabyss.geary.ecs.query.accessors.Accessor

public data class ArchetypeIterator(
    public val archetype: Archetype,
    public val query: Query,
    internal val cachedValues: List<MutableList<Any?>> =
        List(query.accessors.size) { mutableListOf() },
) : Iterator<QueryResult> {
    private val accessors: List<Accessor<*>> = query.accessors

    init {
        if (cachedValues.all { it.isEmpty() })
            accessors.forEachIndexed { i, accessor ->
                for (cached in accessor.cached) {
                    cachedValues[i].add(with(cached) { calculate() })
                }
            }
    }

    private var row: Int = 0

    override fun hasNext(): Boolean = (row < archetype.size || movedRows.isNotEmpty() || combinationsIterator?.hasNext() == true)
        .also { if (!it) archetype.finalizeIterator(this) }

    /** Set of elements moved during a component removal. Represents the resulting row to original row. */
    private val movedRows = mutableSetOf<Int>()

    internal fun addMovedRow(originalRow: Int, resultingRow: Int) {
        if (resultingRow > row) return
        movedRows.remove(originalRow)
        movedRows.add(resultingRow)
    }

    private inner class AccessorCombinationsIterator(val accessorData: AccessorData) : Iterator<List<*>> {
        val data: List<List<Any?>> = accessors.map { with(it) { accessorData.readData() } }
        val combinationsCount = data.fold(1) { acc, b ->
            acc * b.size
        }
        var permutation = 0

        override fun hasNext() = permutation < combinationsCount

        override fun next(): List<*> {
            val permutation = permutation++
            return data.map { it[permutation % it.size] }
        }
    }

    private var combinationsIterator: AccessorCombinationsIterator? = null

    override fun next(): QueryResult {
        if (combinationsIterator?.hasNext() != true) {
            val destinationRow = movedRows.firstOrNull() ?: row++
            movedRows.remove(destinationRow)
            val entity = archetype.ids[destinationRow].toGeary()

            combinationsIterator = AccessorCombinationsIterator(
                AccessorData(
                    archetype = archetype,
                    query = query,
                    row = destinationRow,
                    entity = entity,
                    iterator = this
                )
            )
        }

        return QueryResult(
            entity = combinationsIterator!!.accessorData.entity,
            iterator = this,
            data = combinationsIterator!!.next(),
        )
    }
}

public class AccessorData(
    public val archetype: Archetype,
    public val query: Query,
    public val row: Int,
    public val entity: GearyEntity,
    public val iterator: ArchetypeIterator,
)

public data class QueryResult(
    val entity: GearyEntity,
    internal val iterator: ArchetypeIterator,
    internal val data: List<*>
)
