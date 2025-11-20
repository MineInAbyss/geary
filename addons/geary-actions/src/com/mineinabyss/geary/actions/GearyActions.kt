package com.mineinabyss.geary.actions

import com.mineinabyss.geary.actions.event_binds.bindEntityObservers
import com.mineinabyss.geary.actions.event_binds.parsePassive
import com.mineinabyss.geary.addons.dsl.gearyAddon

val GearyActions = gearyAddon<Unit>("actions") {
    onEnable {
        addCloseables(
            bindEntityObservers(),
            parsePassive()
        )
    }
}
