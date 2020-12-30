package com.mineinabyss.geary.ecs.actions

import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.components.get
import com.mineinabyss.geary.ecs.components.parent
import com.mineinabyss.geary.ecs.components.with
import com.mineinabyss.geary.minecraft.components.PlayerComponent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location

/**
 * Base abstract class for getting a location given an entity.
 *
 * Using polymorphic serialization, we can define and configure subclasses of this class to get locations
 * their own ways.
 *
 * Useful for actions that wish to run at a specific location but want to remain configurable.
 */
@Serializable
public sealed class ConfigurableLocation {
    /** Get a location given an [entity] or null if not applicable. */
    public abstract fun get(entity: GearyEntity): Location?
}

/**
 * Gets the location of the player associated with the entity.
 */
@Serializable
@SerialName("player.location")
public class AtPlayerLocation : ConfigurableLocation() {
    override fun get(entity: GearyEntity): Location? =
        entity.parent?.get<PlayerComponent>()?.player?.location
}

/**
 * Gets the location of the target block the player associated with the entity is looking at.
 *
 * @param maxDist The maximum distance this can extend.
 * @param allowAir Whether to allow clicking on nothing.
 */
@Serializable
@SerialName("player.target_block")
public class AtPlayerTargetBlock(
    private val maxDist: Int = 3,
    private val allowAir: Boolean = true
) : ConfigurableLocation() {
    override fun get(entity: GearyEntity): Location? {
        entity.parent?.with<PlayerComponent> { (player) ->
            val block = player.getTargetBlock(maxDist) ?: return null
            if (!allowAir && block.isEmpty) return null
            return block.location
        }
        return null
    }

}
