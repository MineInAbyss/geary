package com.mineinabyss.geary.datatypes.maps

import com.mineinabyss.geary.datatypes.ULongArrayList

class ULong2ObjectMap<T> {
    @PublishedApi
    internal val objectList = mutableListOf<T>()

    @PublishedApi
    internal val indices = ULongArrayList()

    operator fun set(key: ULong, value: T) {
        val index = indices.indexOf(key)
        if (index != -1) {
            objectList[index] = value
            return
        }
        objectList.add(value)
        indices.add(key)
    }

    operator fun get(key: ULong): T? {
        val index = indices.indexOf(key)
        if (index == -1) return null
        return objectList[index]
    }

    inline fun getOrPut(key: ULong, defaultValue: () -> T): T {
        val index = indices.indexOf(key)
        if (index == -1) {
            val value = defaultValue()
            objectList.add(value)
            indices.add(key)
            return value
        }
        return objectList[index]
    }
}
