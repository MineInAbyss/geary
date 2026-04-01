package com.mineinabyss.geary.actions

import com.mineinabyss.features.feature
import com.mineinabyss.geary.actions.event_binds.bindEntityObservers
import com.mineinabyss.geary.actions.event_binds.parsePassive
import com.mineinabyss.geary.addons.world

val GearyActions = feature("actions") {
    onEnable {
        world {
            bindEntityObservers()
            parsePassive()
        }
    }
}
