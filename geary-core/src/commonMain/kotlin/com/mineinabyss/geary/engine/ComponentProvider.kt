package com.mineinabyss.geary.engine

import com.mineinabyss.geary.datatypes.ComponentId
import kotlin.reflect.KClass

public interface ComponentProvider {
    /**
     * Given a component's [kClass], returns its [ComponentId], or registers an entity
     * with a [ComponentInfo] that will represent this [kClass]'s component type.
     */
    public fun getOrRegisterComponentIdForClass(kClass: KClass<*>): ComponentId
}
