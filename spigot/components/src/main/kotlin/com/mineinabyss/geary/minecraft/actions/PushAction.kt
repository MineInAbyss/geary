package com.mineinabyss.geary.minecraft.actions

import com.mineinabyss.geary.ecs.api.actions.GearyAction
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.minecraft.properties.ConfigurableLocation
import com.mineinabyss.idofront.operators.minus
import com.mineinabyss.idofront.operators.plus
import com.mineinabyss.idofront.operators.times
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.util.Vector

/**
 * Pushes entities away from a [source] location.
 *
 * @param source The location to be pushed away from.
 * @param force A multiplier for the force to be pushed away with
 * @param yOffset An offset for the y component for the final push velocity.
 */
@Serializable
@SerialName("geary:push")
public data class PushAction(
    val source: ConfigurableLocation,
    val force: Double = 1.0,
    val yOffset: Double = 0.0,
) : GearyAction() {
    private val GearyEntity.sourceLoc by source
    private val GearyEntity.bukkitEntity by get<Entity>()

    override fun GearyEntity.run(): Boolean {
        // Knockback resistance goes from 0 to 1 as a percentage where 1 is fully resisted, default is 0
        val resistance = 1 - ((bukkitEntity as? LivingEntity)?.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE)?.value ?: 0.0)

        bukkitEntity.velocity =
            (bukkitEntity.location - sourceLoc).toVector().normalize() * (resistance * force) + Vector(0.0, resistance * yOffset, 0.0)

        return true
    }
}
