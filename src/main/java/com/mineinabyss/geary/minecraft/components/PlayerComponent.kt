package com.mineinabyss.geary.minecraft.components

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.idofront.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import org.bukkit.Bukkit
import java.util.*

@Serializable
class PlayerComponent(
        @Serializable(with = UUIDSerializer::class)
        val uuid: UUID
) : GearyComponent() {
    val player get() = Bukkit.getPlayer(uuid) ?: error("UUID is not a player")

    operator fun component1() = player
}
