package com.mineinabyss.geary.ecs

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
abstract class GearyComponent {
    @Transient
    var persist: Boolean = false
}