package com.mineinabyss.geary.engine.archetypes

import com.mineinabyss.geary.datatypes.ComponentId
import kotlin.reflect.KClass

expect class PlatformSpecificComponentRegistry() {
    fun onRegisterComponent(kClass: KClass<*>, component: ComponentId)
}
