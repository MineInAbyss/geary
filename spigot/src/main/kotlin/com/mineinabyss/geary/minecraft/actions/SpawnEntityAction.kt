package com.mineinabyss.geary.minecraft.actions

import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.actions.GearyAction
import com.mineinabyss.geary.ecs.components.GearyPrefab
import com.mineinabyss.geary.minecraft.spawning.spawnGeary
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location
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
    val prefab: GearyPrefab? = null,
    val at: ConfigurableLocation = AtEntityLocation()
) : GearyAction() {
    public fun spawnAt(location: Location): GearyEntity? {
        return location.spawnGeary(type, prefab)
    }

    override fun runOn(entity: GearyEntity): Boolean {
        val loc = at.get(entity) ?: return false
        return spawnAt(loc) != null
    }
}
