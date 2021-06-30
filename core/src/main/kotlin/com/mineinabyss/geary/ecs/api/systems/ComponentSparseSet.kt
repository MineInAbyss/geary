package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.engine.RELATION
import com.mineinabyss.geary.ecs.engine.get
import com.mineinabyss.geary.ecs.api.relations.toRelation
import com.mineinabyss.geary.ecs.query.*
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.onedaybeard.bitvector.BitVector
import net.onedaybeard.bitvector.bitsOf

private typealias ComponentMap<T> = Long2ObjectOpenHashMap<T>

internal class ComponentSparseSet<T> {
    private val elements: MutableList<T> = mutableListOf()
    private val componentMap = ComponentMap<BitVector>()

    fun GearyComponentId.toComponentMapId(): GearyComponentId =
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
            is ComponentLeaf -> componentMap[family.component] ?: bitsOf()
            is RelationLeaf -> componentMap[family.relationParent.id or RELATION] ?: bitsOf()
        }
    }

    fun match(family: Family): List<T> {
        val matchingArchetypes = mutableListOf<T>()
        getMatchingBits(family, null).forEachBit { matchingArchetypes += elements[it] }
        return matchingArchetypes
    }
}
