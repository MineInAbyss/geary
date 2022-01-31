package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.api.relations.toRelation
import com.mineinabyss.geary.ecs.engine.*
import com.mineinabyss.geary.ecs.query.*
import net.onedaybeard.bitvector.BitVector
import net.onedaybeard.bitvector.bitsOf
import java.util.concurrent.ConcurrentHashMap

/**
 * A map of [GearyComponentId]s to Arrays of objects with the ability to make fast queries based on component IDs.
 */
internal class Component2ObjectArrayMap<T> {
    private val elements = mutableListOf<T>()
    private val elementTypes = mutableListOf<GearyType>()
    private val componentMap = ConcurrentHashMap<Long, BitVector>()

    fun add(element: T, type: GearyType) {
        elements += element
        elementTypes += type
        val index = elements.lastIndex
        type.forEach { id ->
            fun set(i: GearyComponentId) = componentMap.getOrPut(i.toLong()) { bitsOf() }.set(index)

            if (id.isRelation()) {
                val relation = id.toRelation()!!
                // If this is a relation, we additionally set a bit for key/value, so we can make queries with them
                set(relation.id.and(RELATION_VALUE_MASK).withRole(RELATION))
                set(relation.id.and(RELATION_KEY_MASK).withRole(RELATION))
            }
            set(id)
        }
    }

    // Null indicates no bits should be excluded
    private fun getMatchingBits(family: Family, bits: BitVector?): BitVector? {
        fun List<Family>.reduceToBits(operation: BitVector.(BitVector) -> Unit): BitVector? =
            ifEmpty { return null }
                .map { getMatchingBits(it, bits?.copy()) }
                .reduce { acc, andBits ->
                    acc.also {
                        if (andBits != null) it?.operation(andBits)
                    }
                }

        return when (family) {
            is AndSelector -> family.and.reduceToBits(BitVector::and)
            is AndNotSelector -> family.andNot.reduceToBits(BitVector::andNot)
            is OrSelector -> family.or.reduceToBits(BitVector::or)
            is ComponentLeaf -> componentMap.get(family.component.toLong())?.copy() ?: bitsOf()
            is RelationValueLeaf -> {
                // Shift left to match the mask we used above
                val relationId = family.relationValueId.id.shl(32).withRole(RELATION)
                componentMap.get(relationId.toLong())?.copy()?.apply {
                    if (family.componentMustHoldData) {
                        forEachBit { index ->
                            val type = elementTypes[index]
                            if (!type.containsRelationValue(family.relationValueId, componentMustHoldData = true))
                                clear(index)
                        }
                    }
                } ?: bitsOf()
            }
            is RelationKeyLeaf -> {
                val relationId = family.relationKeyId.withRole(RELATION)
                componentMap.get(relationId.toLong())?.copy() ?: bitsOf()
            }
        }
    }

    fun match(family: Family): List<T> {
        val matchingElements = mutableListOf<T>()
        getMatchingBits(family, null)?.forEachBit { matchingElements += elements[it] }
            ?: return elements.toList()

        return matchingElements
    }
}
