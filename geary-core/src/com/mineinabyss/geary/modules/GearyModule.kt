package com.mineinabyss.geary.modules

import org.koin.core.Koin
import org.koin.core.annotation.KoinInternalApi
import org.koin.core.module.Module
import org.koin.dsl.koinApplication
import org.koin.dsl.module

fun geary(
    module: GearyModule,
    koin: Koin = koinApplication().koin,
    configure: GearySetup.() -> Unit = {},
): Geary {
    @OptIn(KoinInternalApi::class)
    koin.apply {
        propertyRegistry.saveProperties(module.properties)
        loadModules(listOf(module.module))
    }
    val initializer = koin.get<EngineInitializer>()
    initializer.init()
    val setup = GearySetup(koin)
    koin.loadModules(listOf(module {
        single<Geary> { setup.geary }
    }))
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
