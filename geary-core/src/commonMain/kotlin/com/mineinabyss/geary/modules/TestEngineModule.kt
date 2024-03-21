package com.mineinabyss.geary.modules

import com.mineinabyss.geary.datatypes.maps.SynchronizedArrayTypeMap
import com.mineinabyss.geary.engine.archetypes.EntityByArchetypeProvider

/**
 * An engine module that initializes the engine but does not start it, useful for testing.
 *
 * No pipeline tasks are run, and the engine won't be scheduled for ticking.
 * Engine ticks may still be called manually.
 */
class TestEngineModule(
    reuseIDsAfterRemoval: Boolean = true,
    useSynchronized: Boolean = false,
) : ArchetypeEngineModule() {
    override val records = if (useSynchronized) SynchronizedArrayTypeMap() else super.records
    override val entityProvider = EntityByArchetypeProvider(reuseIDsAfterRemoval)

    companion object : GearyModuleProviderWithDefault<TestEngineModule> {
        override fun init(module: TestEngineModule) {
            ArchetypeEngineModule.init(module)
        }

        override fun start(module: TestEngineModule) = Unit

        override fun default(): TestEngineModule {
            return TestEngineModule()
        }
    }
}
