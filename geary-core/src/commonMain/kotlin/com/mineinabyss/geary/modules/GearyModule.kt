package com.mineinabyss.geary.modules

import org.koin.core.module.Module
import org.koin.dsl.koinApplication
import org.koin.dsl.module

fun geary(
    module: GearyModule,
    configure: GearySetup.() -> Unit = {},
): UninitializedGearyModule {
    val application = koinApplication {
        properties(module.properties)
        modules(module.module)
    }
    val initializer = application.koin.get<EngineInitializer>()
    initializer.init()
    val setup = GearySetup(application)
    configure(setup)
    return UninitializedGearyModule(setup, initializer)
}

data class UninitializedGearyModule(
    val setup: GearySetup,
    val initializer: EngineInitializer,
) {
    inline fun configure(configure: GearySetup.() -> Unit) = setup.configure()

    fun start(): Geary {
        val world = Geary(setup.application)
        world.addons.initAll(setup)
        initializer.start()
        world.pipeline.runStartupTasks() // TODO keep pipeline separate, it shouldnt be used after init
        return world
    }
}

data class GearyModule(
    val module: Module,
    val properties: Map<String, Any> = emptyMap(),
)
