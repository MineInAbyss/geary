package com.mineinabyss.geary.modules

/**
 * An engine module that does initializes the engine but does not start it.
 *
 * No pipeline tasks are run, and the engine won't be scheduled for ticking.
 * Engine ticks may still be called manually.
 */
object TestEngineModule: GearyModuleProviderWithDefault<ArchetypeEngineModule> {
    override fun init(module: ArchetypeEngineModule) {
        ArchetypeEngineModule.init(module)
    }

    override fun start(module: ArchetypeEngineModule) = Unit

    override fun default(): ArchetypeEngineModule {
        return ArchetypeEngineModule()
    }
}
