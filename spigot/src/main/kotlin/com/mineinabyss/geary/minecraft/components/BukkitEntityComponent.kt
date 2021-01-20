@file:JvmName("BukkitEntityComponentKt")

package com.mineinabyss.geary.minecraft.components

import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.SerializableGearyComponent
import com.mineinabyss.geary.ecs.components.get
import com.mineinabyss.idofront.serialization.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import java.util.*

@Serializable
@SerialName("geary:bukkit_entity_reference")
public class BukkitEntityComponent(
    @Serializable(with = UUIDSerializer::class)
    public val uuid: UUID,
    @Transient
    private val _entity: Entity? = null
): SerializableGearyComponent {
    // Not using lazy here since I think the entity object can stop being the actual entity ingame (ex if player relogs).
    public val entity: Entity
        get() = _entity ?: Bukkit.getEntity(uuid) ?: error("UUID does not link to anything")

    public operator fun component1(): Entity = entity
}

@JvmName("toBukkitEntity")
public fun GearyEntity.toBukkit(): Entity? =
    get<BukkitEntityComponent>()?.entity

public inline fun <reified T : Entity> GearyEntity.toBukkit(): T? =
    get<BukkitEntityComponent>()?.entity as? T
