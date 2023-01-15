package com.mineinabyss.geary.game.components

import com.mineinabyss.idofront.serialization.DurationSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.time.Duration

/**
 * > geary:expiry
 *
 * States something should expire after [duration] time since the component's creation. Commonly used in a relation
 * with other components to remove those components later.
 */
@Serializable
@SerialName("geary:expiry")
class Expiry(
    @Serializable(with = DurationSerializer::class) val duration: Duration
) {
    @Transient
    val endTime: Long = System.currentTimeMillis() + duration.inWholeMilliseconds

    fun timeOver(): Boolean = System.currentTimeMillis() > endTime
    operator fun component1(): Long = endTime
}
