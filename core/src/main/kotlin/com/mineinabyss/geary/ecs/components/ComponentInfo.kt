package com.mineinabyss.geary.ecs.components

import kotlin.reflect.KClass

public data class ComponentInfo(
    val kClass: KClass<*>
)
