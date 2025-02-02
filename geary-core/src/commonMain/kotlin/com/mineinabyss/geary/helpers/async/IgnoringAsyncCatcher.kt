package com.mineinabyss.geary.helpers.async

class IgnoringAsyncCatcher: AsyncCatcher {
    override fun isAsync(): Boolean = false

    override fun throwException(message: String) {
        throw IllegalStateException(message)
    }
}
