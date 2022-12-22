package com.mineinabyss.geary.serialization

import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.serialization.formats.Formats

val serialization by geary.addons.observe<SerializableComponentsModule>()

interface SerializableComponentsModule {
    val serializers: SerializersByMap
    val formats: Formats
}
