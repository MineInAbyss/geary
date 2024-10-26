package com.mineinabyss.geary.engine

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.HOLDS_DATA
import com.mineinabyss.geary.datatypes.NO_ROLE
import com.mineinabyss.geary.datatypes.withRole
import kotlin.reflect.KClassifier
import kotlin.reflect.typeOf

interface ComponentProvider {
    /**
     * Given a component's [kClass], returns its [ComponentId], or registers an entity
     * with a [ComponentInfo] that will represent this [kClass]'s component type.
     */
    fun getOrRegisterComponentIdForClass(kClass: KClassifier): ComponentId
}

inline fun <reified T> ComponentProvider.id(): ComponentId =
    getOrRegisterComponentIdForClass(T::class)

inline fun <reified T> ComponentProvider.idWithNullable(): ComponentId =
    id<T>().withRole(if (typeOf<T>().isMarkedNullable) NO_ROLE else HOLDS_DATA)
