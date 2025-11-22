package com.mineinabyss.geary.test

import com.mineinabyss.geary.engine.archetypes.ArchetypeProvider
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.geary
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.TestInstance
import org.koin.core.Koin
import org.koin.core.component.get

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class GearyTest : Geary {
    private var _koin: Koin? = null
    override fun getKoin(): Koin = _koin!!
    override val closeables: MutableList<AutoCloseable> = mutableListOf()
    override val world: Geary = this

    val rootArchetype get() = get<ArchetypeProvider>().rootArchetype

    open fun setupGeary() = geary(TestEngineModule)

    init {
        startEngine()
    }

    fun startEngine() {
        _koin = setupGeary().getKoin()
    }

    @AfterAll
    fun clearEngine() {
        _koin = null
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
