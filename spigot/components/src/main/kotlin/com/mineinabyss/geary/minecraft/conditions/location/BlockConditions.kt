package com.mineinabyss.geary.minecraft.conditions.location

import com.mineinabyss.geary.ecs.api.conditions.GearyCondition
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location
import org.bukkit.Material

@Serializable
@SerialName("geary:block_type")
public class BlockConditions(
    private val allow: Set<Material> = setOf(),
    private val deny: Set<Material> = setOf()
): GearyCondition() {
    private val GearyEntity.location by get<Location>()

    override fun GearyEntity.check(): Boolean =
        location.add(0.0, -1.0, 0.0).block.type.let {
            (allow.isEmpty() || it in allow) && it !in deny
        }
}
