package com.mineinabyss.geary.context

import com.mineinabyss.geary.engine.Engine
import org.koin.core.component.KoinComponent

public interface EngineContext : KoinComponent {
    public val engine: Engine
}
