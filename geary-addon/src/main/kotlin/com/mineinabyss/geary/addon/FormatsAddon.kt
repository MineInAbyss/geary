package com.mineinabyss.geary.addon

import com.mineinabyss.geary.addon.GearyLoadPhase.REGISTER_FORMATS
import com.mineinabyss.geary.serialization.Formats
import kotlinx.serialization.modules.SerializersModule

fun GearyAddon.formats(init: Formats.(SerializersModule) -> Unit) {
    startup {
        REGISTER_FORMATS {
            formats.init(serializers.module)
        }
    }
}
