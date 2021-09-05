package com.mineinabyss.geary.minecraft.config

import com.mineinabyss.geary.minecraft.GearyPlugin
import com.mineinabyss.idofront.config.IdofrontConfig
import kotlinx.serialization.Serializable

public object GearyConfig : IdofrontConfig<GearyConfig.Data>(GearyPlugin.instance, Data.serializer()) {
    @Serializable
    public class Data(
        public val debug: Boolean = false
    )
}
