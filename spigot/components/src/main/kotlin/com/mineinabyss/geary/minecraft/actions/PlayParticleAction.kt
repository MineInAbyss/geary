package com.mineinabyss.geary.minecraft.actions

import com.mineinabyss.geary.ecs.api.actions.GearyAction
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.minecraft.properties.AtEntityLocation
import com.mineinabyss.geary.minecraft.properties.ConfigurableLocation
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Particle
import org.bukkit.World

/**
 * Spawns a particle
 *
 * @param at The location to spawn the particle at.
 *
 * @see World.spawnParticle
 */
@Serializable
@SerialName("geary:particle")
public data class PlayParticleAction(
    val type: Particle,
    val offsetX: Double = 0.0,
    val offsetY: Double = 0.0,
    val offsetZ: Double = 0.0,
    val count: Int = 1,
    val speed: Double = 0.0,
    // TODO val data: Any? = null, (many things it could be, see if bukkit already has serializer for it)
    val at: ConfigurableLocation = AtEntityLocation(),
) : GearyAction() {
    private val GearyEntity.loc by at

    override fun GearyEntity.run(): Boolean {
        loc.world.spawnParticle(type, loc, count, offsetX, offsetY, offsetZ, speed)
        return true
    }
}
