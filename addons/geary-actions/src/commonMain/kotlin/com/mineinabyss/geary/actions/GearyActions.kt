package com.mineinabyss.geary.actions

import com.mineinabyss.geary.actions.event_binds.bindEntityObservers
import com.mineinabyss.geary.addons.GearyPhase
import com.mineinabyss.geary.addons.dsl.GearyAddonWithDefault
import com.mineinabyss.geary.modules.geary

class GearyActions {
    companion object : GearyAddonWithDefault<GearyActions> {
        override fun default() = GearyActions()

        override fun GearyActions.install() {
            geary.run {
                bindEntityObservers()
            }
        }
    }
}
