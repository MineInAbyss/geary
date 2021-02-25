package com.mineinabyss.geary.ecs.engine

import it.unimi.dsi.fastutil.ints.IntArrayList
import org.clapper.util.misc.SparseArrayList

public class SparseList<E> {
    //TODO implement some iterator of sorts to prevent problems with removing entities while iterating

    internal val packed = mutableListOf<E>()
    internal val unpackedIndices = IntArrayList()
    //TODO Don't know the ArrayList impl too well but access should be O(1), not sure if we can reduce memory consumption in any way
    private val indices = SparseArrayList<Int>()

    public val size: Int get() = packed.size

    public operator fun get(index: Int): E? {
        val packedIndex = indices[index] - 1
        if(packedIndex < 0) return null
        return packed[packedIndex]
    }

    @Synchronized
    public operator fun set(index: Int, value: E) {
        packed.add(value)
        unpackedIndices.add(index)
        indices[index] = packed.size // 1 more than it should be, we subtract in get
    }

    @Synchronized
    public fun remove(index: Int) {
        // place last element of this array into given index
        val packedIndex = indices[index] - 1
        packed[packedIndex] = packed.last()

        // update the packed indices, and indices to reflect the change
        val lastIndex = unpackedIndices.last()
        unpackedIndices[index] = lastIndex
        indices[lastIndex] = packedIndex + 1

        // remove the last element which we just moved over
        unpackedIndices.removeLast()
        packed.removeLast()
    }
}
