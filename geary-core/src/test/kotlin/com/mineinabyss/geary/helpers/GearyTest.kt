package com.mineinabyss.geary.helpers

import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.EngineScope
import com.mineinabyss.geary.ecs.api.systems.QueryManager
import com.mineinabyss.geary.ecs.engine.GearyEngine
import io.kotest.common.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterAll
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get

abstract class GearyTest : KoinTest, EngineScope {
    override val engine get() = get<Engine>() as GearyEngine
    val queryManager get() = get<QueryManager>()

    init {
        runBlocking {
            startKoinWithGeary()
        }
    }

    private suspend fun startKoinWithGeary() {
        val engine = GearyEngine()
        val queryManager = QueryManager(engine)
        @Suppress("RemoveExplicitTypeArguments")
        startKoin {
            modules(module {
                factory<QueryManager> { queryManager }
                factory<Engine> { engine }
            })
        }
        engine.init()
    }

    @AfterAll
    private fun stop() {
        stopKoin()
    }

    /** Recreates the engine. */
    suspend fun clearEngine() {
        stopKoin()
        startKoinWithGeary()
    }
}
