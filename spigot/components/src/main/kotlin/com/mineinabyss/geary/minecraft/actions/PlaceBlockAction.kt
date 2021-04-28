package com.mineinabyss.geary.minecraft.actions

import com.mineinabyss.geary.ecs.api.actions.GearyAction
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Material

/**
 * Summons a block at given location.
 *
 * @param at The location to spawn block at.
 * @param blockType The block to spawn.
 */
@Serializable
@SerialName("geary:placeblock")
public data class PlaceBlockAction(
    val at: ConfigurableLocation,
    val blockType: Material,
) : GearyAction() {
    override fun runOn(entity: GearyEntity): Boolean {
        //TODO Replaces block instead of placing adjacent to it
        val loc = at.get(entity) ?: return false
        loc.block.type = blockType
        return true
    }
}
