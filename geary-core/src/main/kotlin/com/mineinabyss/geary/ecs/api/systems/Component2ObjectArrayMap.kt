package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.api.relations.toRelation
import com.mineinabyss.geary.ecs.engine.RELATION
import com.mineinabyss.geary.ecs.engine.get
import com.mineinabyss.geary.ecs.engine.withRole
import com.mineinabyss.geary.ecs.query.*
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.onedaybeard.bitvector.BitVector
import net.onedaybeard.bitvector.bitsOf

/**
 * A map of [GearyComponentId]s to Arrays of objects with the ability to make fast queries based on component IDs.
 */
internal abstract class Component2ObjectArrayMap<T> {
    private val elements = mutableListOf<T>()
    private val componentMap = Long2ObjectOpenHashMap<BitVector>()

    private fun GearyComponentId.toComponentMapId(): GearyComponentId =
        toRelation()?.parent?.id?.or(RELATION) ?: this

    fun add(element: T, type: GearyType) {
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
                            it.getGearyType().contains(family.relationParent, componentMustHoldData = true)
                        }
                    }
                } ?: bitsOf()
            }
        }
    }

    abstract fun T.getGearyType(): GearyType

    private inline fun BitVector.keepArchetypesMatching(predicate: (archetype: T) -> Boolean): BitVector {
        forEachBit { index ->
            if (!predicate(elements[index]))
                clear(index)
        }
        return this
    }

    fun match(family: Family): List<T> {
        val matchingElements = mutableListOf<T>()
        getMatchingBits(family, null).forEachBit { matchingElements += elements[it] }
        return matchingElements
    }
}