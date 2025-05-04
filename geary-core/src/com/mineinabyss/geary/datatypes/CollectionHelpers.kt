package com.mineinabyss.geary.datatypes

import androidx.collection.LongSparseArray
import androidx.collection.getOrElse

fun <T> LongSparseArray<T>.getOrPut(key: Long, defaultValue: () -> T): T {
    return getOrElse(key) { defaultValue().also { put(key, it) } }
}
