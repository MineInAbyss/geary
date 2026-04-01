package com.mineinabyss.geary.modules

import com.mineinabyss.features.FeatureManager
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance

fun geary(
    module: GearyModule,
    extendDI: DI? = null,
    configure: GearySetup.() -> Unit = {},
): Geary {
//    val di = DI {
//    }
//    val initializer = di.direct.instance<EngineInitializer>()
//    initializer.init()
    val withGeary = DI.direct {
        extendDI?.let { extend(it) }
        import(module.module)
//        extend(di)
        bindSingleton<Geary> { Geary(this.directDI) }
        bindSingleton { FeatureManager(this.di) }
    }
    withGeary.instance<EngineInitializer>().init()
    val setup = GearySetup(withGeary)
    configure(setup)
    return Geary(withGeary)
}

data class GearyModule(
    val module: DI.Module,
) {
    fun withOverrides(builder: DI.Builder.() -> Unit): GearyModule {
        return GearyModule(DI.Module(module.name, allowSilentOverride = true) {
            import(module)
            builder()
        })
    }
}

