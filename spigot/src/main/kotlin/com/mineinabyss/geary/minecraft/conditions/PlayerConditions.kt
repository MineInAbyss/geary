package com.mineinabyss.geary.minecraft.conditions

import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.components.get
import com.mineinabyss.geary.ecs.components.parent
import com.mineinabyss.geary.ecs.conditions.GearyCondition
import com.mineinabyss.geary.minecraft.components.PlayerComponent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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
    private infix fun <T> T?.nullOrEquals(other: T?): Boolean =
        this == null || this == other

    override fun conditionsMet(entity: GearyEntity): Boolean {
        val (player) = entity.parent?.get<PlayerComponent>() ?: return false
        return conditionsMet(player)
    }

    public fun conditionsMet(player: Player): Boolean =
        isSneaking nullOrEquals player.isSneaking &&
                isSprinting nullOrEquals player.isSprinting
}
