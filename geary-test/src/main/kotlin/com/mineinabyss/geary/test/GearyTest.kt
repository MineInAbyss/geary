package com.mineinabyss.geary.test

import com.mineinabyss.geary.engine.Engine
import com.mineinabyss.geary.engine.archetypes.ArchetypeProvider
import com.mineinabyss.geary.modules.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.TestInstance
import org.koin.core.KoinApplication

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class GearyTest : Geary {
    private var _application: KoinApplication? = null
    override val application get() = _application!!
    val rootArchetype get() = get<ArchetypeProvider>().rootArchetype

    open fun setupGeary() = geary(TestEngineModule)

    init {
        startEngine()
    }

    fun startEngine(override: UninitializedGearyModule? = null) {
        _application = (override ?: setupGeary()).start().application
    }

    @AfterAll
    fun clearEngine() {
        _application = null
    }

    /** Recreates the engine. */
    fun resetEngine(override: UninitializedGearyModule? = null) {
        clearEngine()
        startEngine(override)
    }

    companion object {
        suspend inline fun <T> concurrentOperation(
            times: Int = 10000,
            crossinline run: suspend (id: Int) -> T,
        ): List<Deferred<T>> {
            return withContext(Dispatchers.Default) {
                (0 until times).map { id ->
                    async { run(id) }
                }
            }
        }
    }
}
