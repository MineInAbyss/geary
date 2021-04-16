package com.mineinabyss.geary.ecs.components

import com.mineinabyss.geary.ecs.api.autoscan.AutoscanComponent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@AutoscanComponent
@SerialName("geary:debug")
public class DebugComponent(
    public val message: String
) {
    public val timestamp: Long = System.currentTimeMillis()
}
