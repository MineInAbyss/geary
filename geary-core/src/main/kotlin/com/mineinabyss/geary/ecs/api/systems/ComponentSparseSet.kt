package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.api.relations.toRelation
import com.mineinabyss.geary.ecs.engine.*
import com.mineinabyss.geary.ecs.query.*
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.onedaybeard.bitvector.BitVector
import net.onedaybeard.bitvector.bitsOf

private typealias ComponentMap<T> = Long2ObjectOpenHashMap<T>

internal class ComponentSparseSet {
    private val elements: MutableList<Archetype> = mutableListOf()
    private val componentMap = ComponentMap<BitVector>()

    private fun GearyComponentId.toComponentMapId(): GearyComponentId =
        toRelation()?.parent?.id?.or(RELATION) ?: this

    fun add(element: Archetype, type: GearyType) {
        elements += element
        val index = elements.lastIndex
        type.map { it.toComponentMapId() }.forEach { id ->
            componentMap.getOrPut(id.toLong()) { bitsOf() }.set(index)
        }
    }

    private fun getMatchingBits(family: Family, bits: BitVector?): BitVector {
        fun List<Family>.reduceToBits(operation: BitVector.(BitVector) -> Unit): BitVector =
            ifEmpty { return bitsOf() }
                .map { getMatchingBits(it, bits?.copy()) }
                .reduce { acc, andBits -> acc.also { it.operation(andBits) } }

        return when (family) {
            is AndSelector -> family.and.reduceToBits(BitVector::and)
            is AndNotSelector -> family.andNot.reduceToBits(BitVector::andNot)
            is OrSelector -> family.or.reduceToBits(BitVector::or)
            is ComponentLeaf -> componentMap[family.component]?.copy() ?: bitsOf()
            is RelationLeaf -> {
                val relationId = family.relationParent.id.withRole(RELATION)
                componentMap[relationId]?.copy()?.apply {
                    if (family.componentMustHoldData) {
                        keepArchetypesMatching {
                            it.dataHoldingRelations.containsKey(family.relationParent.id.toLong())
                        }
                    }
                } ?: bitsOf()
            }
        }
    }

    private inline fun BitVector.keepArchetypesMatching(predicate: (archetype: Archetype) -> Boolean): BitVector {
        forEachBit { index ->
            if (!predicate(elements[index]))
                clear(index)
        }
        return this
    }

    fun match(family: Family): List<Archetype> {
        val matchingArchetypes = mutableListOf<Archetype>()
        getMatchingBits(family, null).forEachBit { matchingArchetypes += elements[it] }
        return matchingArchetypes
    }
}
