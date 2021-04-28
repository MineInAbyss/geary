package com.mineinabyss.geary.ecs.components

import com.mineinabyss.geary.ecs.api.autoscan.AutoscanComponent
import com.mineinabyss.idofront.time.TimeSpan
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * > geary:expiry
 *
 * States something should expire after [duration] time since the component's creation. Commonly used in a relation
 * with other components to remove those components later.
 */
@Serializable
@SerialName("geary:expiry")
@AutoscanComponent
public class Expiry(
    public val duration: TimeSpan
) {
    @Transient
    public val endTime: Long = System.currentTimeMillis() + duration.inMillis

    public fun timeOver(): Boolean = System.currentTimeMillis() > endTime
    public operator fun component1(): Long = endTime
}
