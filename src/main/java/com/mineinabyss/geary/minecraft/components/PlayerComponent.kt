package com.mineinabyss.geary.minecraft.components

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.idofront.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

@Serializable
public class PlayerComponent(
        @Serializable(with = UUIDSerializer::class)
        public val uuid: UUID
) : GearyComponent {
    public val player: Player get() = Bukkit.getPlayer(uuid) ?: error("UUID is not a player")

    public operator fun component1(): Player = player
}
