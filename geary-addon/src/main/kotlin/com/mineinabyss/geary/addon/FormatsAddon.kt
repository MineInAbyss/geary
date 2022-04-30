package com.mineinabyss.geary.addon

import com.mineinabyss.geary.addon.GearyLoadPhase.REGISTER_FORMATS
import com.mineinabyss.geary.serialization.GearyFormats
import kotlinx.serialization.modules.SerializersModule

public fun GearyAddon.formats(init: GearyFormats.(SerializersModule) -> Unit) {
    startup {
        REGISTER_FORMATS {
            formats.init(serializers.module)
        }
    }
}
