@file:UseSerializers(
    DoubleRangeSerializer::class
)

package com.mineinabyss.geary.minecraft.conditions

import com.mineinabyss.geary.ecs.api.conditions.GearyCondition
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.components.parent
import com.mineinabyss.geary.minecraft.components.PlayerComponent
import com.mineinabyss.idofront.serialization.DoubleRangeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.bukkit.entity.Player

/**
 * Conditions that can be checked against a [Player].
 */
//TODO add more!
@Serializable
@SerialName("player")
public class PlayerConditions(
    public val isSneaking: Boolean? = null,
    public val isSprinting: Boolean? = null,
) : GearyCondition {

    override fun conditionsMet(entity: GearyEntity): Boolean {
        val (player) = entity.parent?.get<PlayerComponent>() ?: return false
        return conditionsMet(player)
    }

    public fun conditionsMet(player: Player): Boolean =
        isSneaking nullOrEquals player.isSneaking &&
                isSprinting nullOrEquals player.isSprinting
}

internal infix fun <T> T?.nullOrEquals(other: T?): Boolean =
    this == null || this == other

internal inline infix fun <T> T?.nullOr(check: (T) -> Boolean): Boolean =
    this == null || check(this)
