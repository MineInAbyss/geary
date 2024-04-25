package com.mineinabyss.geary.datatypes

private const val bucketSize: Int = 1024

class BucketedULongArray {
    private val buckets = mutableListOf<LongArray>()
    var maxSupportedSize = 0
        private set
    var size = 0
        private set

    val lastIndex get() = size - 1

    operator fun get(index: Int): ULong {
        val bucketIndex = index / bucketSize
        val bucket = buckets[bucketIndex]
        return bucket[index % bucketSize].toULong()
    }

    fun ensureSize(including: Int) {
        var maxSupportedSize = maxSupportedSize
        while (including >= maxSupportedSize) {
            buckets.add(LongArray(bucketSize))
            maxSupportedSize += bucketSize
        }
        this.maxSupportedSize = maxSupportedSize
    }

    operator fun set(index: Int, value: ULong) {
        val bucketIndex = index / bucketSize
        ensureSize(index)
        val bucket = buckets[bucketIndex]
        if (index >= size) size = index + 1
        bucket[index % bucketSize] = value.toLong()
    }

    fun add(value: ULong) {
        val index = size
        set(index, value)
    }

    fun getAll(): ULongArray {
        return ULongArray(size) { get(it) }
    }

    fun removeLastOrNull(): ULong? {
        if (size == 0) return null
        return get(lastIndex).also {
            size--
            if (size % bucketSize == 0) buckets.removeLast()
        }
    }
}
