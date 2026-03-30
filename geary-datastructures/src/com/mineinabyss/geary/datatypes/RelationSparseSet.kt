package com.mineinabyss.geary.datatypes

import androidx.collection.MutableIntList
import androidx.collection.mutableObjectListOf
import kotlin.jvm.JvmField

/**
 * A sparse set which stores extra data for a target entity.
 */
class RelationSparseSet<E> {
    @PublishedApi
    @JvmField
    internal val denseData = mutableObjectListOf<E>().apply {
        add(null as E)
    }

    @PublishedApi
    @JvmField
    internal val entityIds = MutableIntList().apply {
        add(0)
    }

    @PublishedApi
    @JvmField
    internal val sparseIndices = PagedIntArray()


    @PublishedApi
    @JvmField
    internal val sparseRelationTargets = PagedIntArray()

    val size get() = entityIds.size

    operator fun get(entityId: Int, target: Int): E? {
        val denseIndex = sparseIndices[entityId]
        if (denseIndex == 0) return null
        return denseData[denseIndex]
    }

    operator fun set(entityId: Int, target: Int, data: E) {
        val denseIndex = sparseIndices[entityId]
        val targetOffset = sparseRelationTargets[entityId]
        if (denseIndex == 0) {
            denseData.add(data)
            entityIds.add(entityId)
            sparseIndices[entityId] = denseData.lastIndex
            sparseRelationTargets[entityId] = 1
            return
        }
        if (targetOffset == 0) {

        }
        denseData[denseIndex] = data
    }

    operator fun contains(entityId: Int): Boolean = sparseIndices[entityId] != 0

    fun remove(entityId: Int) {
        val denseIndex = sparseIndices[entityId]
        if (denseIndex == 0) return
        val lastIndex = denseData.lastIndex
        val lastData = denseData[lastIndex]
        val lastEntityId = entityIds[lastIndex]
        denseData[denseIndex] = lastData
        entityIds[denseIndex] = lastEntityId
        denseData.removeAt(lastIndex)
        entityIds.removeAt(lastIndex)
        sparseIndices.remove(entityId)
        sparseIndices[lastEntityId] = denseIndex
    }

    inline fun forEachIndexed(action: (Int, E) -> Unit) {
        for (i in 1..denseData.lastIndex) {
            action(entityIds[i], denseData[i])
        }
    }
}

fun main() {
    val set = SparseSet<String>()
    set[1] = "hi"
    set[8000] = " world"
    set[8001] = "!"
    set.remove(8000)
    set.remove(8002)
    println(set[1] + set[8000] + set[8001])
    set.forEachIndexed { index, value -> println("$index: $value") }
}