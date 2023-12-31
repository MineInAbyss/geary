package com.mineinabyss.geary.engine.archetypes

import com.mineinabyss.geary.datatypes.ComponentId
import kotlin.reflect.KClass

actual class PlatformSpecificComponentRegistry {
    actual fun onRegisterComponent(kClass: KClass<*>, component: ComponentId) = Unit
}
