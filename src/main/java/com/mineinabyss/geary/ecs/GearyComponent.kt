package com.mineinabyss.geary.ecs

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
public abstract class GearyComponent {
    //TODO consider a better way to mark things as persistent
    // which doesn't involve serializing this value
    public var persist: Boolean = false
        internal set
}
