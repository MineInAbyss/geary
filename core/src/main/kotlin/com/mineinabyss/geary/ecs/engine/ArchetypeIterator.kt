package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.geary
import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.query.Query

internal data class ArchetypeIterator(
    private val archetype: Archetype,
    private val query: Query,

    private val dataIndices: List<Int> = query.dataKey
        .filter { it and HOLDS_DATA != 0uL }
        .map { archetype.indexOf(it) },

    private val matchedRelations: Map<GearyComponentId, List<Relation>> =
        archetype.matchedRelationsFor(query.relationsKey),

    private val relationCombinations: List<RelationCombination> =
        matchedRelations.values.fold(emptyList()) { curr, components: List<Relation> ->
            val componentIndices = components.map { archetype.indexOf(it.id) }

            if (curr.isEmpty())
                listOf(RelationCombination(components, componentIndices.toIntArray()))
            else {
                // Add each individual component to every combination from before. Flatten the resulting list of lists.
                components.flatMapIndexed { i, relation ->
                    val compIndex = componentIndices[i]
                    curr.map { combination ->
                        RelationCombination(
                            combination.relations + relation,
                            combination.componentIndices + compIndex
                        )
                    }
                }
            }
        }
) : Iterator<QueryResult> {
    var row = 0

    override fun hasNext() = (row < archetype.size || movedRows.isNotEmpty())
        .also { if (!it) archetype.finalizeIterator(this) }

    private var relationCombinationsIterator: Iterator<RelationCombination> = relationCombinations.iterator()
    private var componentData = listOf<GearyComponent>()

    /** Set of elements moved during a component removal. Represents the resulting row to original row. */
    private val movedRows = mutableSetOf<Int>()

    fun addMovedRow(originalRow: Int, resultingRow: Int) {
        if(resultingRow > row) return
        movedRows.remove(originalRow)
        movedRows.add(resultingRow)
    }

    override fun next(): QueryResult {
        // Find the row we'll be reading data from. If a row was moved due to another row's removal
        //TODO is there a more efficient way of taking any element from the set and removing it?
        val destinationRow = movedRows.firstOrNull() ?: row++
        movedRows.remove(destinationRow)
        val entity = geary(archetype.ids[destinationRow])

        return if (matchedRelations.isNotEmpty()) {
            if (!relationCombinationsIterator.hasNext()) {
                relationCombinationsIterator = relationCombinations.iterator()
                componentData = getAtIndex(destinationRow)
            }
            val combination = relationCombinationsIterator.next()
            val relationData = combination.componentIndices.map { index ->
                archetype.componentData[index][destinationRow]
            }
            QueryResult(entity, componentData, combination.relations.map { it.component }, relationData)
        } else {
            componentData = getAtIndex(destinationRow)
            QueryResult(entity, componentData)
        }
    }

    private fun getAtIndex(index: Int) = dataIndices.map {
        archetype.componentData[it][index]
    }
}

public class RelationCombination(
    public val relations: List<Relation>,
    public val componentIndices: IntArray,
)

public data class QueryResult(
    val entity: GearyEntity,
    val data: List<GearyComponent>,
    val relationCompIds: List<GearyComponentId> = listOf(),
    val relationCompData: List<GearyComponent> = listOf(),
)
