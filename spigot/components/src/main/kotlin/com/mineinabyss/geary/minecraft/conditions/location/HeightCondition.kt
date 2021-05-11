package com.mineinabyss.geary.minecraft.conditions.location

import com.mineinabyss.geary.ecs.api.conditions.GearyCondition
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location

@Serializable
@SerialName("geary:height")
public class HeightCondition(
    private val min: Int = 0,
    private val max: Int = 256,
) : GearyCondition() {
    private val GearyEntity.location by get<Location>()

    override fun GearyEntity.check(): Boolean =
        location.y.toInt() in min..max
}
