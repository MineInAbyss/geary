package com.mineinabyss.geary.ecs.components

import com.mineinabyss.geary.ecs.api.autoscan.AutoscanComponent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * > geary:debug
 *
 * Holds a [message] with a [timestamp] for when the class was instantiated. Used for debugging.
 */
@Serializable
@SerialName("geary:debug")
@AutoscanComponent
public class DebugComponent(
    public val message: String
) {
    public val timestamp: Long = System.currentTimeMillis()
}
