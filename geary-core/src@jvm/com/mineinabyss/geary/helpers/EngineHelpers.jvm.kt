package com.mineinabyss.geary.helpers

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.modules.WorldScoped

actual inline fun <reified T> WorldScoped.componentId(): ComponentId {
    return world
        .componentProvider
        .getOrRegisterComponentIdForClass(T::class.java)
}