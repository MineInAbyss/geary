@file:UseSerializers(
    DoubleRangeSerializer::class
)

package com.mineinabyss.geary.minecraft.conditions

import com.mineinabyss.geary.ecs.api.conditions.GearyCondition
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.idofront.serialization.DoubleRangeSerializer
import com.mineinabyss.idofront.util.DoubleRange
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

/**
 * Conditions that can be checked against a [Player].
 */
//TODO add more!
@Serializable
@SerialName("geary:health")
public class HealthConditions(
    public val within: DoubleRange? = null,
    public val withinPercent: DoubleRange? = null,
) : GearyCondition() {
    private val GearyEntity.entity by get<Entity>()

    override fun GearyEntity.check(): Boolean {
        val living = entity as? LivingEntity ?: return false

        return within nullOr { living.health in it }
                && withinPercent nullOr {
            living.health / (living.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: return false) in it
        }
    }
}
