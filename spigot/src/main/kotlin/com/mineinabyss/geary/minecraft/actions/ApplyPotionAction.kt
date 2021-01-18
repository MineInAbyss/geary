package com.mineinabyss.geary.minecraft.actions

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.actions.GearyAction
import com.mineinabyss.geary.minecraft.store.BukkitEntityAccess
import com.mineinabyss.geary.minecraft.store.bukkit
import com.mineinabyss.idofront.serialization.PotionEffectSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.LivingEntity
import org.bukkit.potion.PotionEffect
import kotlin.random.Random

@Serializable
@SerialName("geary:apply_potion_effects")
public data class ApplyPotionAction(
    val effects: List<@Serializable(with = PotionEffectSerializer::class) PotionEffect>,
    val applyChance: Double = 1.0,
) : GearyAction(){
    override fun runOn(entity: GearyEntity): Boolean {
        if (Random.nextDouble() <= applyChance) {
            (bukkit(entity) as? LivingEntity)?.addPotionEffects(effects) ?: return false
        }
        return true
    }
}