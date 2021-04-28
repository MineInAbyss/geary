package com.mineinabyss.geary.minecraft.actions

import com.mineinabyss.geary.ecs.api.actions.GearyAction
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.minecraft.access.toBukkit
import com.mineinabyss.idofront.serialization.PotionEffectSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.LivingEntity
import org.bukkit.potion.PotionEffect
import kotlin.random.Random

/**
 * Applies potion effects to the target entity with a certain (optional) chance.
 *
 * @param effects The potion effects to apply.
 * @param applyChance Chance of applying the effects.
 */
@Serializable
@SerialName("geary:apply_potion_effects")
public data class ApplyPotionAction(
    val effects: List<@Serializable(with = PotionEffectSerializer::class) PotionEffect>,
    val applyChance: Double = 1.0,
) : GearyAction() {
    //TODO by toBukkit?
    override fun GearyEntity.run(): Boolean {
        if (Random.nextDouble() <= applyChance) {
            toBukkit<LivingEntity>()?.addPotionEffects(effects) ?: return false
        }
        return true
    }
}
