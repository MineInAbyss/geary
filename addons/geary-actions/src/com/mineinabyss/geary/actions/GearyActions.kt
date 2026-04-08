package com.mineinabyss.geary.actions

import com.mineinabyss.dependencies.gets
import com.mineinabyss.geary.actions.event_binds.bindEntityObservers
import com.mineinabyss.geary.actions.event_binds.parsePassive
import com.mineinabyss.geary.addons.gearyAddon

val GearyActions = gearyAddon("actions") {
    bindEntityObservers()
    parsePassive()
}.gets<String>()
