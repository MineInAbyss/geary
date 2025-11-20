package com.mineinabyss.geary.serialization

import com.mineinabyss.geary.addons.dsl.gearyAddon
import org.koin.core.module.dsl.scopedOf
import org.koin.dsl.bind

val SerializableComponents = gearyAddon<SerializableComponentsModule>("serializeable-components") {
    scopedModule {
        scopedOf(::SerializersByMap) bind ComponentSerializers::class
        scopedOf(::SerializationFormats)
        scopedOf(::SerializableComponentsModule)
    }
}
