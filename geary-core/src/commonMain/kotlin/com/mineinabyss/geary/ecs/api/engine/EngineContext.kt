package com.mineinabyss.geary.ecs.api.engine

import org.koin.core.component.KoinComponent

public interface EngineContext : KoinComponent {
    public val engine: Engine
}
