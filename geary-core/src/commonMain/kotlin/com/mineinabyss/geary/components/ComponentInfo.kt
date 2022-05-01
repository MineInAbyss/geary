package com.mineinabyss.geary.components

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.reflect.KClassifier

/**
 * Represents the [class][kClass] a component entity is responsible for.
 */
@Serializable
@SerialName("geary:component_info")
public data class ComponentInfo(
    val kClass: KClassifier
)
