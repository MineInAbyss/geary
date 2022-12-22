package com.mineinabyss.geary.serialization

import com.mineinabyss.geary.modules.geary

val serialization = geary.addons.observe<SerializableComponentsModule>()

interface SerializableComponentsModule {
    val serializers: SerializersByMap
    val formats: Formats
}
