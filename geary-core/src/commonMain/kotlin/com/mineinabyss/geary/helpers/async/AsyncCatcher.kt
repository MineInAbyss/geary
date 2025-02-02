package com.mineinabyss.geary.helpers.async

interface AsyncCatcher {
    fun isAsync(): Boolean

    fun throwException(message: String)
}

inline fun AsyncCatcher.catch(message: () -> String) {
    if (isAsync()) throwException(message())
}
