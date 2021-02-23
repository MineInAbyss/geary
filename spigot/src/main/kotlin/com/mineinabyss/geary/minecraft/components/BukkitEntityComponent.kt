package com.mineinabyss.geary.minecraft.components

import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.components.get
import org.bukkit.entity.Entity

//TODO convert into a custom serializer
/*@Serializable
@SerialName("geary:bukkit_entity_reference")
@AutoscanComponent
public class BukkitEntityComponent(
    @Serializable(with = UUIDSerializer::class)
    public val uuid: UUID,
    @Transient
    private val _entity: Entity? = null
) {
    // Not using lazy here since I think the entity object can stop being the actual entity ingame (ex if player relogs).
    public val entity: Entity
        get() = _entity ?: Bukkit.getEntity(uuid) ?: error("UUID does not link to anything")

    public operator fun component1(): Entity = entity
}*/

@JvmName("toBukkitEntity")
public fun GearyEntity.toBukkit(): Entity? =
    get<Entity>()

public inline fun <reified T : Entity> GearyEntity.toBukkit(): T? =
    get<Entity>() as? T
