package com.mineinabyss.geary.papermc

import kotlinx.serialization.Serializable

@Serializable
public class GearyConfig(
    public val debug: Boolean = false,
    public val webConsole: Boolean = true,
)
