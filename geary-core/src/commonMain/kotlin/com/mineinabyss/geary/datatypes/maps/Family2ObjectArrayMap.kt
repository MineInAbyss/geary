package com.mineinabyss.geary.datatypes.maps

import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.helpers.hasRelationKind
import com.mineinabyss.geary.helpers.hasRelationTarget
import com.mineinabyss.geary.modules.geary

/**
 * A map of [ComponentId]s to Arrays of objects with the ability to make fast queries based on component IDs.
 */
internal class Family2ObjectArrayMap<T>(
    val getIndex: ((T) -> Int)? = null,
    val setIndex: ((T, Int) -> Unit)? = null
) {
    private val _elements = mutableListOf<T>()
    private val elementTypes = mutableListOf<EntityType>()

    val elements: List<T> get() = _elements

    /**
     * A map of component ids to a [BitSet] where each set bit means that the element at its index in [_elements]
     * contains this component in its type.
     *
     * ### Extra rules are applied for relations:
     * For relations with a kind or type equal to the [Any] component, the [BitSet] represents elements whose type
     * contains at least one relation of that kind/type to any other type/kind.
     */
    private val componentMap = mutableMapOf<Long, BitSet>()

    fun add(element: T, type: EntityType) {
        set(element, type, _elements.size)
    }

    internal fun set(element: T, type: EntityType, index: Int) {
        if (index == _elements.size) {
            _elements.add(element)
            elementTypes.add(type)
        } else {
            _elements[index] = element
            elementTypes[index] = type
        }
        type.forEach { id ->
            fun set(i: ComponentId) = componentMap.getOrPut(i.toLong()) { bitsOf() }.set(index)

            // See componentMap definition for relations
            if (id.isRelation()) {
                val relation = Relation.of(id)
                set(Relation.of(relation.kind, geary.components.any).id)
                set(Relation.of(geary.components.any, relation.target).id)
            }
            set(id)
        }
        setIndex?.invoke(element, index)
    }

    private fun clearBits(type: EntityType, index: Int) {
        type.forEach { id ->
            fun clear(i: ComponentId) = componentMap[i.toLong()]?.clear(index)

            // See componentMap definition for relations
            if (id.isRelation()) {
                val relation = Relation.of(id)
                clear(Relation.of(relation.kind, geary.components.any).id)
                clear(Relation.of(geary.components.any, relation.target).id)
            }
            clear(id)
        }
    }

    internal fun remove(element: T) {
        val index = getIndex?.invoke(element) ?: _elements.indexOf(element)
         // Clear data for current element
        val type = elementTypes[index]

        clearBits(type, index)

        if(index == _elements.lastIndex) {
            _elements.removeAt(index)
            elementTypes.removeAt(index)
            return
        }

        val lastElement = _elements.last()
        val lastType = elementTypes.last()

        clearBits(lastType, _elements.lastIndex)

        // copy data from last element
        set(lastElement, lastType, index)

        // remove last element
        _elements.removeAt(_elements.lastIndex)
        elementTypes.removeAt(elementTypes.lastIndex)
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
                (bits ?: bitsOf().apply { set(0, _elements.lastIndex) }).apply {
                    andNot(inside)
                }
            }

            is Family.Selector.Or -> family.or.reduceToBits(BitSet::or)
            is Family.Leaf.Component -> componentMap[family.component.toLong()]?.copy() ?: bitsOf()
            is Family.Leaf.AnyToTarget -> {
                // The bits for relationId in componentMap represent archetypes with any relations containing target
                val relationId = Relation.of(geary.components.any, family.target).id
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
                val relationId = Relation.of(family.kind, geary.components.any).id
                componentMap[relationId.toLong()]?.copy()?.apply {
                    if (family.targetMustHoldData) forEachBit { index ->
                        val type = elementTypes[index]
                        if (!type.hasRelationKind(family.kind, targetMustHoldData = true))
                            clear(index)
                    }
                } ?: bitsOf()
            }
        }
    }

    fun match(family: Family): List<T> {
        val bits = getMatchingBits(family, null) ?: return _elements.toList()
        val matchingElements = ArrayList<T>(bits.cardinality)
        bits.forEachBit { matchingElements += _elements[it] }
        return matchingElements
    }
}
