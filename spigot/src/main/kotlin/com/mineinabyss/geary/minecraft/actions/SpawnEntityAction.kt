package com.mineinabyss.geary.minecraft.actions

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.actions.GearyAction
import com.mineinabyss.geary.minecraft.spawnGeary
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType

/**
 * Applies potion effects to the target entity with a certain (optional) chance.
 *
 * @param effects The potion effects to apply.
 * @param applyChance Chance of applying the effects.
 */
@Serializable
@SerialName("geary:spawn")
public data class SpawnEntityAction(
    val type: EntityType,
    val components: List<@Polymorphic GearyComponent> = listOf(),
    val at: ConfigurableLocation = AtEntityLocation()
) : GearyAction() {
    public fun spawnAt(location: Location): Entity? {
        return location.spawnGeary(type, components)
    }

    override fun runOn(entity: GearyEntity): Boolean {
        val loc = at.get(entity) ?: return false
        return spawnAt(loc) != null
    }
}
