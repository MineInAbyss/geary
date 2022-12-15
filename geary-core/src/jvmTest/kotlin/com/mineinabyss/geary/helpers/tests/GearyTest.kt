package com.mineinabyss.geary.helpers.tests

import com.mineinabyss.geary.context.GearyArchetypeModule
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.AfterAll
import kotlin.time.Duration.Companion.milliseconds

abstract class GearyTest {
    lateinit var geary: GearyArchetypeModule
        private set

    init {
        clearEngine()
    }

    fun startEngine() {
        val module = GearyArchetypeModule(tickDuration = 20.milliseconds)
        geary = module
        module.inject()
        module.engine.start()
    }

    /** Recreates the engine. */
    fun clearEngine() {
        startEngine()
    }

    @AfterAll
    private fun stop() {
        clearEngine()
    }

    suspend inline fun concurrentOperation(
        times: Int = 10000,
        crossinline run: suspend (id: Int) -> Unit
    ): List<Deferred<*>> {
        return withContext(Dispatchers.Default) {
            (0 until times).map { id ->
                async { run(id) }
            }
        }
    }
}
