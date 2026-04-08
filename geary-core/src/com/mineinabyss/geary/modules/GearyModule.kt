package com.mineinabyss.geary.modules

import com.mineinabyss.dependencies.*

inline fun geary(
    module: DI.Module,
    configure: Geary.() -> Unit = {},
): Geary {
    val scope = scope {
        import(submodule(module))
    }
    val geary = scope.get<Geary>()
    configure(geary)
    return geary
}
