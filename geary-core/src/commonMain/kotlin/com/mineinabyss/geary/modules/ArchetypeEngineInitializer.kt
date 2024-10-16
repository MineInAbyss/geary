package com.mineinabyss.geary.modules

import com.mineinabyss.geary.engine.Pipeline
import com.mineinabyss.geary.engine.archetypes.ArchetypeEngine
import com.mineinabyss.geary.engine.archetypes.ComponentAsEntityProvider

class ArchetypeEngineInitializer(
    val beginTickingOnStart: Boolean,
    private val pipeline: Pipeline,
    private val engine: ArchetypeEngine,
) : EngineInitializer {
    override fun init() {
    }

    override fun start() {
        pipeline.runStartupTasks()
        if (beginTickingOnStart) engine.start()
    }
}
