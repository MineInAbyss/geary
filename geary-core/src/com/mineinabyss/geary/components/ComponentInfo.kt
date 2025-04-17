package com.mineinabyss.geary.components

import kotlin.reflect.KClassifier

/**
 * Represents the [class][kClass] a component entity is responsible for.
 */
data class ComponentInfo(
    val kClass: KClassifier
)
