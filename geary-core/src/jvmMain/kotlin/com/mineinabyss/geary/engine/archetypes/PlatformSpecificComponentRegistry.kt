package com.mineinabyss.geary.engine.archetypes

import com.mineinabyss.geary.datatypes.ComponentDefinition
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.helpers.toGeary
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObject

actual class PlatformSpecificComponentRegistry {
    actual fun onRegisterComponent(kClass: KClass<*>, component: ComponentId) {
        val compDef = kClass.companionObject?.objectInstance as? ComponentDefinition ?: return
        compDef.onCreate(component.toGeary())
    }
}
