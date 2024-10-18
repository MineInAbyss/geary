package com.mineinabyss.geary.actions

import com.mineinabyss.geary.actions.event_binds.bindEntityObservers
import com.mineinabyss.geary.actions.event_binds.parsePassive
import com.mineinabyss.geary.addons.dsl.createAddon

val GearyActions = createAddon("Geary Actions") {
    onStart {
        bindEntityObservers()
        parsePassive()
    }
}
