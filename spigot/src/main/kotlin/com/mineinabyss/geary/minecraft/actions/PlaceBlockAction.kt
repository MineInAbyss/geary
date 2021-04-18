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
 * @param block The block to spawn.
 */
@Serializable
@SerialName("geary:placeblock")

public data class PlaceBlockAction(
    val at: ConfigurableLocation,
    val block: Material,
) : GearyAction() {
    override fun runOn(entity: GearyEntity): Boolean {
        val loc = at.get(entity) ?: return false
        //TODO Make it summon ontop/underneath/adjacent to block
        //     aka where air. Not only when block is air.
        if (loc.world.getBlockAt(loc).type == Material.AIR)
            loc.world.getBlockAt(loc).type = block
        return true
    }
}
