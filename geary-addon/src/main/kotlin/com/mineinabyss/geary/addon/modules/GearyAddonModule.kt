package com.mineinabyss.geary.addon.modules

import com.mineinabyss.ding.DI
import com.mineinabyss.geary.addon.GearyAddonManager
import com.mineinabyss.geary.serialization.Serializers

val addons: GearyAddonModule by DI.observe()

interface GearyAddonModule {
    val manager: GearyAddonManager

    fun inject() {
        DI.add(this)
    }
}
