package com.mineinabyss.geary.modules

import com.mineinabyss.geary.engine.archetypes.EntityByArchetypeProvider

/**
 * An engine module that does initializes the engine but does not start it.
 *
 * No pipeline tasks are run, and the engine won't be scheduled for ticking.
 * Engine ticks may still be called manually.
 */
class TestEngineModule(
    reuseIDsAfterRemoval: Boolean = true,
): ArchetypeEngineModule() {
    override val entityProvider = EntityByArchetypeProvider(reuseIDsAfterRemoval)

    companion object: GearyModuleProviderWithDefault<TestEngineModule> {
        override fun init(module: TestEngineModule) {
            ArchetypeEngineModule.init(module)
        }

        override fun start(module: TestEngineModule) = Unit

        override fun default(): TestEngineModule {
            return TestEngineModule()
        }
    }
}
