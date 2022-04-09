package com.mineinabyss.geary.ecs.components

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.reflect.KClassifier

@Serializable
@SerialName("geary:component_info")
public data class ComponentInfo(
    val kClass: KClassifier
)
