package com.mineinabyss.geary.ecs.api.engine

import org.koin.core.component.KoinComponent

public interface EngineScope: KoinComponent {
    public val engine: Engine
}
