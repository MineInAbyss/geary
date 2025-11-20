package com.mineinabyss.geary.modules

import org.koin.core.module.Module
import org.koin.dsl.koinApplication

fun geary(
    module: GearyModule,
    configure: GearySetup.() -> Unit = {},
): Geary {
    val application = koinApplication {
        properties(module.properties)
        modules(module.module)
    }
    val initializer = application.koin.get<EngineInitializer>()
    initializer.init()
    val setup = GearySetup(application)
    configure(setup)
    return setup.geary
}

//data class UninitializedGearyModule(
//    val setup: GearySetup,
//    val initializer: EngineInitializer,
//) {
//    inline fun configure(configure: GearySetup.() -> Unit): UninitializedGearyModule = apply { setup.configure() }
//
//    fun start(): Geary {
//        val world = Geary(setup.application)
//        initializer.start()
//        return world
//    }
//}

data class GearyModule(
    val module: Module,
    val properties: Map<String, Any> = emptyMap(),
)
