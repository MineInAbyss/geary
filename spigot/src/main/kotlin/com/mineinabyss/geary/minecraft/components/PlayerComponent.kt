package com.mineinabyss.geary.minecraft.components

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.idofront.serialization.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

@Serializable
@SerialName("geary:player_reference")
public class PlayerComponent(
    @Serializable(with = UUIDSerializer::class)
    public val uuid: UUID
) : GearyComponent {
    // Not using lazy here since I think the entity object can stop being the actual entity ingame (ex if player relogs).
    public val player: Player
        get() = Bukkit.getPlayer(uuid) ?: error("UUID does not link to a player")

    public operator fun component1(): Player = player
}
