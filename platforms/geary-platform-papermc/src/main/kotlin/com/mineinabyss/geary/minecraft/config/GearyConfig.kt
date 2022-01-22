package com.mineinabyss.geary.minecraft.config

import com.mineinabyss.geary.minecraft.GearyPlugin
import com.mineinabyss.geary.webconsole.GearyWebConsole
import com.mineinabyss.idofront.config.IdofrontConfig
import com.mineinabyss.idofront.config.ReloadScope
import kotlinx.serialization.Serializable

public object GearyConfig : IdofrontConfig<GearyConfig.Data>(GearyPlugin.instance, Data.serializer()) {
    @Serializable
    public class Data(
        public val debug: Boolean = false,
        public val webConsole: Boolean = true,
    )

    private var webConsole: GearyWebConsole? = null

    override fun ReloadScope.load() {
        if (data.webConsole)
            "Starting web console" {
                webConsole = GearyWebConsole()
                webConsole?.start()
            }
    }

    override fun ReloadScope.unload() {
        webConsole?.apply {
            "Stopping web console" {
                stop()
                webConsole = null
            }
        }
    }
}
