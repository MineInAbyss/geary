package com.mineinabyss.geary.context

import com.mineinabyss.geary.engine.Components
import com.mineinabyss.geary.engine.Engine
import com.mineinabyss.geary.serialization.GearyFormats
import com.mineinabyss.geary.serialization.GearySerializers
import com.mineinabyss.geary.systems.QueryManager
import org.koin.core.component.inject

public open class GearyContextKoin : GearyContext {
    override val engine: Engine by inject()
    override val formats: GearyFormats by inject()
    override val serializers: GearySerializers by inject()
    override val queryManager: QueryManager by inject()
    override val components: Components by inject()

    public companion object {
        public inline operator fun <T> invoke(run: GearyContextKoin.() -> T): T {
            return GearyContextKoin().run(run)
        }
    }
}
