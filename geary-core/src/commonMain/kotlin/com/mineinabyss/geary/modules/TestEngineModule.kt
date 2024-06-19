package com.mineinabyss.geary.modules

import com.mineinabyss.geary.addons.ApplicationFactory
import com.mineinabyss.geary.datatypes.maps.SynchronizedArrayTypeMap
import com.mineinabyss.geary.engine.archetypes.EntityByArchetypeProvider

/**
 * An engine module that initializes the engine but does not start it, useful for testing.
 *
 * No pipeline tasks are run, and the engine won't be scheduled for ticking.
 * Engine ticks may still be called manually.
 */
class TestEngineModule(
    config: ArchetypeEngineConfig
) : ArchetypeEngineModule(config) {
    companion object : ApplicationFactory<TestEngineModule, ArchetypeEngineConfig> {
        override fun create(configure: ArchetypeEngineConfig.() -> Unit): TestEngineModule {
            val config = ArchetypeEngineConfig().apply(configure)
            return TestEngineModule(config).apply {
                ArchetypeEngineModule.init(this)
            }
        }
    }
}
