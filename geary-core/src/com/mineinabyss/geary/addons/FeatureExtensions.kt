package com.mineinabyss.geary.addons

import com.mineinabyss.features.FeatureDI
import com.mineinabyss.features.addCloseables
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.WorldScoped
import org.kodein.di.instance

inline fun FeatureDI.world(block: WorldScoped.() -> Unit) {
    val scope = instance<Geary>().newScope(this)
    try {
        scope.apply(block)
    } finally {
        addCloseables(scope)
    }
}