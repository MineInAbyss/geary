package com.mineinabyss.geary.datatypes.maps

import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.hasRelationKind
import com.mineinabyss.geary.helpers.hasRelationTarget

/**
 * A map of [GearyComponentId]s to Arrays of objects with the ability to make fast queries based on component IDs.
 */
internal class Family2ObjectArrayMap<T> {
    private val elements = mutableListOf<T>()
    private val elementTypes = mutableListOf<GearyType>()

    /**
     * A map of component ids to a [BitSet] where each set bit means that the element at its index in [elements]
     * contains this component in its type.
     *
     * ### Extra rules are applied for relations:
     * For relations with a kind or type equal to the [Any] component, the [BitSet] represents elements whose type
     * contains at least one relation of that kind/type to any other type/kind.
     */
    private val componentMap = mutableMapOf<Long, BitSet>()

    fun add(element: T, type: GearyType) {
        elements += element
        elementTypes += type
        val index = elements.lastIndex
        type.forEach { id ->
            fun set(i: GearyComponentId) = componentMap.getOrPut(i.toLong()) { bitsOf() }.set(index)

            // See componentMap definition for relations
            if (id.isRelation()) {
                val relation = id.toRelation()!!
                set(Relation.of(relation.kind, componentId<Any>()).id)
                set(Relation.of(componentId<Any>(), relation.target).id)
            }
            set(id)
        }
    }

    /**
     * @param bits When null, indicates no bits should be excluded (i.e. a bitset of all 1s)
     */
    private fun getMatchingBits(family: Family, bits: BitSet?): BitSet? {
        fun List<Family>.reduceToBits(operation: BitSet.(BitSet) -> Unit): BitSet? =
            ifEmpty { return null }.map { getMatchingBits(it, bits?.copy()) }
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
            is Family.Leaf.AnyToTarget -> {
                // The bits for relationId in componentMap represent archetypes with any relations containing target
                val relationId = Relation.of(componentId<Any>(), family.target).id
                componentMap[relationId.toLong()]?.copy()?.apply {
                    if (family.kindMustHoldData) forEachBit { index ->
                        val type = elementTypes[index]
                        if (!type.hasRelationTarget(family.target, kindMustHoldData = true))
                            clear(index)
                    }
                } ?: bitsOf()
            }
            is Family.Leaf.KindToAny -> {
                // The bits for relationId in componentMap represent archetypes with any relations containing kind
                val relationId = Relation.of(family.kind, componentId<Any>()).id
                componentMap[relationId.toLong()]?.copy()?.apply {
                    if (family.targetMustHoldData) forEachBit { index ->
                        val type = elementTypes[index]
                        if (!type.hasRelationKind(family.kind, targetMustHoldData = true))
                            clear(index)
                    }
                } ?: bitsOf()
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
