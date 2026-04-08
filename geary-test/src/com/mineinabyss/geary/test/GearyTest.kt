package com.mineinabyss.geary.test

import com.mineinabyss.dependencies.DIContext
import com.mineinabyss.dependencies.get
import com.mineinabyss.geary.engine.archetypes.ArchetypeProvider
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.WorldScoped
import com.mineinabyss.geary.modules.geary
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class GearyTest : WorldScoped {
    private var _world: Geary? = null
    override val world: Geary get() = _world!!
    override val di: DIContext get() = world.di

    val rootArchetype get() = get<ArchetypeProvider>().rootArchetype

    open fun setupGeary() = geary(TestEngineModule)

    init {
        startEngine()
    }

    fun startEngine() {
        _world = setupGeary()
    }

    @AfterAll
    fun clearEngine() {
        _world = null
    }

    /** Recreates the engine. */
    fun resetEngine() {
        clearEngine()
        startEngine()
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
