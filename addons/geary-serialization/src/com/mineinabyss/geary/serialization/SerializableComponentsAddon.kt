package com.mineinabyss.geary.serialization

import com.mineinabyss.features.feature
import org.kodein.di.bindSingletonOf
import org.kodein.di.delegate

val SerializableComponents = feature<SerializableComponentsModule>("serializeable-components") {
    dependencies {
        bindSingletonOf(::SerializersByMap)
        delegate<ComponentSerializers>().to<SerializersByMap>()
        bindSingletonOf(::SerializationFormats)
        bindSingletonOf(::SerializableComponentsModule)
    }
}
