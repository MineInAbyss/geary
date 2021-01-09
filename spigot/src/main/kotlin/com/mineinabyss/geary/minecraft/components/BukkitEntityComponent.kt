package com.mineinabyss.geary.minecraft.components

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.idofront.serialization.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.entity.Mob
import org.bukkit.entity.Player
import java.util.*

public typealias PlayerComponent = BukkitEntityComponent<Player>
public typealias MobComponent = BukkitEntityComponent<Mob>

/**
 * Component for players registered with the ECS.
 *
 * @param uuid The UUID of the player
 */
@Serializable
@SerialName("geary:bukkit_entity_reference")
public class BukkitEntityComponent<T : Entity>(
    @Serializable(with = UUIDSerializer::class)
    public val uuid: UUID,
    @Transient
    private val _entity: T? = null
) : GearyComponent {
    public val entity: T get() = _entity ?: (Bukkit.getEntity(uuid) as? T) ?: error("UUID does not link to anything")

    public operator fun component1(): T = entity
}
