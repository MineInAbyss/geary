package com.mineinabyss.geary.api.addon

import com.mineinabyss.geary.serialization.GearyFormats
import kotlinx.serialization.modules.SerializersModule

public fun GearyAddon.formats(init: GearyFormats.(SerializersModule) -> Unit) {
    startup {
        GearyLoadPhase.REGISTER_FORMATS {
            formats.init(serializers.module)
        }
    }
}
