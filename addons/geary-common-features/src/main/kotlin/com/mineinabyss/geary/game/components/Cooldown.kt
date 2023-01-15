package com.mineinabyss.geary.game.components

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("geary:cooldown")
class Cooldown(
    val length: Long
) {
    val endTime: Long = System.currentTimeMillis() + length
}
