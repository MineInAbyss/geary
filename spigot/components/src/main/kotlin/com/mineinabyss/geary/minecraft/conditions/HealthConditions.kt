@file:UseSerializers(
    DoubleRangeSerializer::class
)

package com.mineinabyss.geary.minecraft.conditions

import com.mineinabyss.geary.ecs.api.conditions.GearyCondition
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.minecraft.access.toBukkit
import com.mineinabyss.idofront.serialization.DoubleRangeSerializer
import com.mineinabyss.idofront.util.DoubleRange
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.bukkit.attribute.Attribute
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

/**
 * Conditions that can be checked against a [Player].
 */
//TODO add more!
@Serializable
@SerialName("health")
public class HealthConditions(
    public val within: DoubleRange? = null,
    public val withinPercent: DoubleRange? = null,
) : GearyCondition {
    override fun conditionsMet(entity: GearyEntity): Boolean {
        val bukkit = entity.toBukkit<LivingEntity>() ?: return false

        return within nullOr { bukkit.health in it }
                && withinPercent nullOr {
                bukkit.health / (bukkit.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: return false) in it
        }
    }
}
