package com.mineinabyss.geary.minecraft.actions

import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.actions.GearyAction
import com.mineinabyss.geary.minecraft.components.toBukkit
import com.mineinabyss.idofront.serialization.DoubleRangeSerializer
import com.mineinabyss.idofront.util.DoubleRange
import com.mineinabyss.idofront.util.randomOrMin
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.LivingEntity

/**
 * Deals damage to the target entity.
 *
 * @param damage The damage amount.
 */
@Serializable
@SerialName("geary:deal_damage")
public data class DealDamageAction(
    val damage: @Serializable(with = DoubleRangeSerializer::class) DoubleRange
) : GearyAction() {
    override fun runOn(entity: GearyEntity): Boolean {
        entity.toBukkit<LivingEntity>()?.damage(damage.randomOrMin()) ?: return false
        return true
    }
}
