package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.geary
import com.mineinabyss.geary.ecs.api.relations.Relation

internal class ArchetypeIterator(
    private val archetype: Archetype,
    private val dataKey: List<GearyComponentId>,
    private val relationsKey: List<Relation>,
) : Iterator<ArchetypeIterationResult> {
    init {
        archetype.movedRows.clear()
    }

    private val dataIndices = dataKey
        .filter { it and HOLDS_DATA != 0uL }
        .map { archetype.indexOf(it) }

    private val relationDataIndices = relationsKey.map { archetype.indexOf(it.id) }

    private val matchedRelations = archetype.matchedRelationsFor(relationsKey)

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

    private var row = 0

    override fun hasNext() = (row < archetype.size || archetype.movedRows.isNotEmpty())
        .also { if (!it) archetype.movedRows.clear() }

    private var relationCombinationsIterator: Iterator<RelationCombination> = relationCombinations.iterator()
    private var componentData = listOf<GearyComponent>()

    override fun next(): ArchetypeIterationResult {
        // Find the row we'll be reading data from. If a row was moved due to another row's removal
        //
        //TODO is there a more efficient way of taking any element from the set and removing it?
        val destinationRow = archetype.movedRows.firstOrNull() ?: row++
        archetype.movedRows.remove(destinationRow)
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
            ArchetypeIterationResult(entity, componentData, combination.relations.map { it.component }, relationData)
        } else {
            componentData = getAtIndex(destinationRow)
            ArchetypeIterationResult(entity, componentData)
        }
    }

    private fun getAtIndex(index: Int) = dataIndices.map {
        archetype.componentData[it][index]
    }

    /**
     * Resets this iterator. Useful as creating it every time is relatively expensive,
     * as we need to do slow indexOf operations.
     */
    internal fun reset() {
        //TODO we should only be adding stuff here if something is currently iterating over this archetype
        // we'll remove this line eventually
        archetype.movedRows.clear()
        row = 0
        relationCombinationsIterator = relationCombinations.iterator()
        componentData = listOf()
    }
}

public class RelationCombination(
    public val relations: List<Relation>,
    public val componentIndices: IntArray,
)

public data class ArchetypeIterationResult(
    val entity: GearyEntity,
    val data: List<GearyComponent>,
    val relationCompIds: List<GearyComponentId> = listOf(),
    val relationCompData: List<GearyComponent> = listOf(),
)
