package com.mineinabyss.geary.ecs.helpers

import com.mineinabyss.geary.ecs.api.GearyContext
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.systems.QueryManager
import com.mineinabyss.geary.ecs.serialization.Formats
import org.koin.core.component.inject

public open class GearyContextKoin : GearyContext {
    override val engine: Engine by inject()
    override val formats: Formats by inject()
    override val queryManager: QueryManager by inject()

    public companion object {
        public inline operator fun <T> invoke(run: GearyContextKoin.() -> T): T {
            return GearyContextKoin().run(run)
        }
    }
}
