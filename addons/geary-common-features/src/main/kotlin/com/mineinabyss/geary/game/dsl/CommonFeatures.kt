package com.mineinabyss.geary.game.dsl

import com.mineinabyss.geary.addons.dsl.GearyAddonWithDefault
import com.mineinabyss.geary.game.systems.ExpiringComponentSystem
import com.mineinabyss.geary.modules.geary

class CommonFeatures {
    companion object : GearyAddonWithDefault<Unit> {
        override fun default() = Unit

        override fun Unit.install() {
            geary.pipeline.addSystems(
                ExpiringComponentSystem()
            )
        }
    }
}
