package com.mineinabyss.geary.minecraft.actions

import com.mineinabyss.geary.ecs.api.actions.GearyAction
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Spawns a lightning bolt.
 *
 * @param at The location to smite at.
 */
@Serializable
@SerialName("geary:smite")
public data class SmiteAction(
    val at: ConfigurableLocation
) : GearyAction() {
    override fun runOn(entity: GearyEntity): Boolean {
        val loc = at.get(entity) ?: return false
        loc.world.strikeLightning(loc)
        return true
    }
}
