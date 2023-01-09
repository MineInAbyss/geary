package com.mineinabyss.geary.serialization.dsl

import com.mineinabyss.geary.datatypes.Component
import com.mineinabyss.geary.serialization.ComponentSerializers
import com.mineinabyss.geary.serialization.SerializersByMap
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.overwriteWith
import kotlin.reflect.KClass

class ComponentSerializersBuilder {
    val modules = mutableListOf<SerializersModule>()
    val serialNameToClass = mutableMapOf<String, KClass<out Component>>()

    fun build(): ComponentSerializers = SerializersByMap(
        modules.fold(EmptySerializersModule()) { acc, module ->
            acc.overwriteWith(module)
        },
        serialNameToClass.toMap()
    )
}
