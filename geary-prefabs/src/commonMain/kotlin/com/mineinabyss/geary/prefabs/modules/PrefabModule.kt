package com.mineinabyss.geary.prefabs.modules

import com.mineinabyss.ding.DI
import com.mineinabyss.geary.prefabs.PrefabManager

val prefabs: PrefabModule by DI.observe()

interface PrefabModule {
    val manager: PrefabManager

    fun inject() {
        DI.add(this)
    }
}
