package com.mineinabyss.geary.helpers.tests

import com.mineinabyss.geary.modules.GearyArchetypeModule
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.idofront.di.DI
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import kotlin.time.Duration.Companion.milliseconds

abstract class GearyTest {
    lateinit var geary: GearyArchetypeModule
        private set

    init {
        startEngine()
    }

    fun startEngine() {
        val module = GearyArchetypeModule(tickDuration = 20.milliseconds)
        DI.add<GearyArchetypeModule>(module)
        DI.add<GearyModule>(module)
        module.inject()
        geary = module
    }

    @AfterAll
    fun clearEngine() {
        DI.clear()
    }

    /** Recreates the engine. */
    fun resetEngine() {
        clearEngine()
        startEngine()
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
