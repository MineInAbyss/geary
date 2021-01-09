package com.mineinabyss.geary.ecs.actions.conditions

import kotlinx.serialization.Serializable

/**
 * Conditions that can be checked against a [Player].
 */
@Serializable
public class PlayerConditions(
    public val isSneaking: Boolean? = null,
    public val isSprinting: Boolean? = null,
) {
    private infix fun <T> T?.nullOrEquals(other: T?): Boolean =
        this == null || this == other

//    public fun conditionsMet(player: Player): Boolean =
//        isSneaking nullOrEquals player.isSneaking &&
//                isSprinting nullOrEquals player.isSprinting
}
