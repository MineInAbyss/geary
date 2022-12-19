package com.mineinabyss.geary.addon.modules

import com.mineinabyss.ding.DI
import com.mineinabyss.geary.addons.AddonManager

val addons: AddonModule by DI.observe()

interface AddonModule {
    val manager: AddonManager

    fun inject() {
        DI.add(this)
    }
}
