package com.mineinabyss.geary.ecs.components

import kotlin.reflect.KClass

/**
 * Wrapper for kClass used to register components in the engine.
 */
public data class ComponentInfo(
    val kClass: KClass<*>
)
