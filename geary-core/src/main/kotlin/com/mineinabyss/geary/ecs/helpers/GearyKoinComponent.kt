package com.mineinabyss.geary.ecs.helpers

import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.EngineContext
import com.mineinabyss.geary.ecs.api.systems.QueryManager
import org.koin.core.component.inject

public open class GearyKoinComponent : EngineContext {
    public override val engine: Engine by inject()
    public val queryManager: QueryManager by inject()

    public companion object {
        public inline operator fun <T> invoke(run: GearyKoinComponent.() -> T): T {
            return GearyKoinComponent().run(run)
        }
    }
}
