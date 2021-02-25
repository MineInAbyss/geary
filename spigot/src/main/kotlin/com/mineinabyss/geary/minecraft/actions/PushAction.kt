package com.mineinabyss.geary.minecraft.actions

import com.mineinabyss.geary.ecs.api.actions.GearyAction
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.minecraft.access.toBukkit
import com.mineinabyss.idofront.operators.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.attribute.Attribute
import org.bukkit.entity.LivingEntity
import org.bukkit.util.Vector
import java.util.*

/**
 * Deals damage to the target entity.
 *
 * @param damage The damage amount.
 */
@Serializable
@SerialName("geary:push")
public data class PushAction(
    val source: ConfigurableLocation,
    val force: Double = 1.0,
    val yOffset: Double = 0.0,
) : GearyAction() {
    override fun runOn(entity: GearyEntity): Boolean {
        val bukkitEntity = entity.toBukkit() ?: return false
        val sourceLoc = source.get(entity) ?: return false

        // Knockback resistance goes from 0 to 1 as a percentage where 1 is fully resisted, default is 0
        val resistance = 1 - ((bukkitEntity as? LivingEntity)?.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE)?.value ?: 0.0)

        bukkitEntity.velocity =
            (bukkitEntity.location - sourceLoc).toVector().normalize() * (resistance * force) + Vector(0.0, resistance * yOffset, 0.0)

        return true
    }
}
