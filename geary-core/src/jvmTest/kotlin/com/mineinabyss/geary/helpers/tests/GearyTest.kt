package com.mineinabyss.geary.helpers.tests

import com.mineinabyss.geary.context.EngineContext
import com.mineinabyss.geary.context.GearyContextKoin
import com.mineinabyss.geary.context.QueryContext
import com.mineinabyss.geary.context.globalContext
import com.mineinabyss.geary.engine.Engine
import com.mineinabyss.geary.engine.GearyEngine
import com.mineinabyss.geary.systems.QueryManager
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.AfterAll
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.logger.Logger
import org.koin.core.logger.PrintLogger
import org.koin.dsl.module
import kotlin.time.Duration.Companion.milliseconds

abstract class GearyTest : KoinComponent, EngineContext {
    override val engine get() = get<Engine>() as GearyEngine
    val queryManager get() = get<QueryManager>()

    init {
        clearEngine()
    }

    private fun startKoinWithGeary() {
        with(object : QueryContext {
            override val queryManager = QueryManager()
        }) {
            val engine = GearyEngine(10.milliseconds)
            @Suppress("RemoveExplicitTypeArguments")
            startKoin {
                modules(module {
                    single<Logger> { PrintLogger() }
                    factory<QueryManager> { queryManager }
                    factory<Engine> { engine }
                })
            }
            globalContext = GearyContextKoin()
            engine.init()
            queryManager.init()
        }
    }

    @AfterAll
    private fun stop() {
        stopKoin()
    }

    /** Recreates the engine. */
    fun clearEngine() {
        stopKoin()
        startKoinWithGeary()
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
