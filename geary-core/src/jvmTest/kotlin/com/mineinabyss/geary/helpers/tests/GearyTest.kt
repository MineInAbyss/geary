package com.mineinabyss.geary.helpers.tests

import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.idofront.di.DI
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.AfterAll

abstract class GearyTest {
    private var _geary: Geary? = null
    val geary get() = _geary!!

    init {
        startEngine()
    }

    fun startEngine() {
        _geary = geary(TestEngineModule).start()
    }

    @AfterAll
    fun clearEngine() {
        DI.clear()
        _geary = null
    }

    /** Recreates the engine. */
    fun resetEngine() {
        clearEngine()
        startEngine()
    }

    companion object {
        suspend inline fun <T> concurrentOperation(
            times: Int = 10000,
            crossinline run: suspend (id: Int) -> T
        ): List<Deferred<T>> {
            return withContext(Dispatchers.Default) {
                (0 until times).map { id ->
                    async { run(id) }
                }
            }
        }
    }
}
