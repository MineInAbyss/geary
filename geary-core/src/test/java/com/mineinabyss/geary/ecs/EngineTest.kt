package com.mineinabyss.geary.ecs

import com.mineinabyss.geary.ecs.accessors.GearyAccessorScope
import com.mineinabyss.geary.ecs.engine.GearyEngine

abstract class EngineTest(engine: GearyEngine) : GearyAccessorScope(engine) {
}