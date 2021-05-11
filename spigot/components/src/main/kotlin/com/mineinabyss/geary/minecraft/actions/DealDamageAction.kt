package com.mineinabyss.geary.minecraft.actions

import com.mineinabyss.geary.ecs.api.actions.GearyAction
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.minecraft.access.toBukkit
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
    val damage: @Serializable(with = DoubleRangeSerializer::class) DoubleRange,
    val minHealth: Double = 0.0,
    val ignoreArmor: Boolean = false,
) : GearyAction() {
    override fun GearyEntity.run(): Boolean {
        val bukkit = toBukkit<LivingEntity>() ?: return false
        //if true, damage dealt ignores armor, otherwise factors armor into damage calc
        if (ignoreArmor) bukkit.health = (bukkit.health - damage.randomOrMin()).coerceAtLeast(minHealth) else bukkit.damage(damage.randomOrMin());
        return true
    }
}
