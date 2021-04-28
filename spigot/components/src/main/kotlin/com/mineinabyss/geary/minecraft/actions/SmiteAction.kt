package com.mineinabyss.geary.minecraft.actions

import com.mineinabyss.geary.ecs.api.actions.GearyAction
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.minecraft.properties.ConfigurableLocation
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
    private val GearyEntity.loc by at

    override fun GearyEntity.run(): Boolean {
        loc.world.strikeLightning(loc)
        return true
    }
}
