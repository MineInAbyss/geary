package com.mineinabyss.geary.engine

import com.mineinabyss.geary.datatypes.ComponentId
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KType

interface ComponentProvider {
    /**
     * Given a component's [kClass], returns its [ComponentId], or registers an entity
     * with a [ComponentInfo] that will represent this [kClass]'s component type.
     */
    fun getOrRegisterComponentIdForClass(kClass: KClassifier): ComponentId
}
