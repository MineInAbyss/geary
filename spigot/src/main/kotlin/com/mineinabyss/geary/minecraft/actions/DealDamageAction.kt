package com.mineinabyss.geary.minecraft.actions

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.actions.GearyAction
import com.mineinabyss.geary.minecraft.store.BukkitEntityAccess
import com.mineinabyss.geary.minecraft.store.bukkit
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.LivingEntity

@Serializable
@SerialName("geary:deal_damage")
public data class DealDamageAction(
    val damage : Double
) : GearyAction() {
    override fun runOn(entity: GearyEntity): Boolean {
        (bukkit(entity) as? LivingEntity)?.damage(damage) ?: return false
        return true
    }
}