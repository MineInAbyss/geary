package com.mineinabyss.geary.modules

import com.mineinabyss.features.FeatureManager
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.direct
import org.kodein.di.instance

fun geary(
    module: GearyModule,
    extendDI: DI? = null,
    configure: GearySetup.() -> Unit = {},
): Geary {
    val di = DI {
        extendDI?.let { extend(it) }
        import(module.module)
    }
    val initializer = di.direct.instance<EngineInitializer>()
    initializer.init()
    val withGeary = DI {
        extend(di)
        bindSingleton<Geary> { Geary(this.di) }
        bindSingleton { FeatureManager(this.di) }
    }
    val setup = GearySetup(withGeary)
    configure(setup)
    return Geary(withGeary)
}

data class GearyModule(
    val module: DI.Module,
)
