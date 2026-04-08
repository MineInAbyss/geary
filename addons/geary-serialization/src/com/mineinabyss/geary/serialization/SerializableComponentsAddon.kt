package com.mineinabyss.geary.serialization

import com.mineinabyss.dependencies.gets
import com.mineinabyss.dependencies.new
import com.mineinabyss.dependencies.single
import com.mineinabyss.geary.addons.gearyAddon
import com.mineinabyss.geary.modules.Geary

val SerializableComponents = gearyAddon("serializeable-components") {
    single<ComponentSerializers> { new(::SerializersByMap) }
    single { new(::SerializationFormats) }
    single { new(::SerializableComponentsModule) }
}.gets<SerializableComponentsModule>()

fun Geary.serialization(configure: SerializableComponentsModule.() -> Unit) =
    scope.load(SerializableComponents, configure)
