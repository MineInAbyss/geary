package com.mineinabyss.geary.ecs

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
public abstract class GearyComponent {
    @Transient
    public var persist: Boolean = false
        internal set
}
