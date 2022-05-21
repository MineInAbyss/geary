package com.mineinabyss.geary.datatypes.maps

import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.helpers.hasRelationTarget

/**
 * A map of [GearyComponentId]s to Arrays of objects with the ability to make fast queries based on component IDs.
 */
internal class Component2ObjectArrayMap<T> {
    private val elements = mutableListOf<T>()
    private val elementTypes = mutableListOf<GearyType>()
    private val componentMap = mutableMapOf<Long, BitSet>()

    fun add(element: T, type: GearyType) {
        elements += element
        elementTypes += type
        val index = elements.lastIndex
        type.forEach { id ->
            fun set(i: GearyComponentId) = componentMap.getOrPut(i.toLong()) { bitsOf() }.set(index)

            if (id.isRelation()) {
                val relation = id.toRelation()!!
                // If this is a relation, we additionally set a bit for key/value, so we can make queries with them
                set(relation.id.and(RELATION_TARGET_MASK).withRole(RELATION))
                set(relation.id.and(RELATION_KIND_MASK).withRole(RELATION))
            }
            set(id)
        }
    }

    // Null indicates no bits should be excluded
    private fun getMatchingBits(family: Family, bits: BitSet?): BitSet? {
        // If family empty, consider it as matching everything
//        if (family is Family.Selector && family.elements.isEmpty()) return null

        fun List<Family>.reduceToBits(operation: BitSet.(BitSet) -> Unit): BitSet? =
            ifEmpty { return null }.
            map { getMatchingBits(it, bits?.copy()) }
                .reduce { acc: BitSet?, andBits: BitSet? ->
                    acc.also {
                        if (andBits != null) it?.operation(andBits)
                    }
                }

        return when (family) {
            is Family.Selector.And -> family.and.reduceToBits(BitSet::and)
            is Family.Selector.AndNot -> {
                // We take current bits and removed any matched inside, if null is returned, all bits are removed
                val inside = family.andNot.reduceToBits(BitSet::or) ?: return bitsOf()
                (bits ?: bitsOf().apply { set(0, elements.lastIndex) }).apply {
                    andNot(inside)
                }
            }
            is Family.Selector.Or -> family.or.reduceToBits(BitSet::or)
            is Family.Leaf.Component -> componentMap[family.component.toLong()]?.copy() ?: bitsOf()
            is Family.Leaf.RelationTarget -> {
                val relationId = family.relationTargetId.withRole(RELATION)
                componentMap[relationId.toLong()]?.copy()?.apply {
                    if (family.componentMustHoldData) {
                        forEachBit { index ->
                            val type = elementTypes[index]
                            if (!type.hasRelationTarget(family.relationTargetId, componentMustHoldData = true))
                                clear(index)
                        }
                    }
                } ?: bitsOf()
            }
            is Family.Leaf.RelationKind -> {
                // Shift left to match the mask we used above
                val relationId = family.relationKindId.shl(32).withRole(RELATION)
                componentMap[relationId.toLong()]?.copy() ?: bitsOf()
            }
            else -> TODO("Kotlin compiler is shitting itself")
        }
    }

    fun match(family: Family): List<T> {
        val matchingElements = mutableListOf<T>()
        getMatchingBits(family, null)?.forEachBit { matchingElements += elements[it] }
            ?: return elements.toList()

        return matchingElements
    }
}
