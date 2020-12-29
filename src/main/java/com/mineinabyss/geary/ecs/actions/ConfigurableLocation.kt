package com.mineinabyss.geary.ecs.actions

import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.components.get
import com.mineinabyss.geary.ecs.components.parent
import com.mineinabyss.geary.ecs.components.with
import com.mineinabyss.geary.minecraft.components.PlayerComponent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location

@Serializable
public sealed class ConfigurableLocation {
    public abstract fun get(entity: GearyEntity): Location?
}

@Serializable
@SerialName("player.location")
public class AtPlayerLocation : ConfigurableLocation() {
    override fun get(entity: GearyEntity): Location? =
        entity.parent?.get<PlayerComponent>()?.player?.location
}


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
