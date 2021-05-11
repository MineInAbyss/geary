package com.mineinabyss.geary.minecraft.actions

import com.mineinabyss.geary.ecs.api.actions.GearyAction
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.minecraft.properties.AtPlayerLocation
import com.mineinabyss.geary.minecraft.properties.ConfigurableLocation
import com.mineinabyss.idofront.spawning.spawn
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.TNTPrimed

@Serializable
@SerialName("geary:explode")
public class Explode(
    private val at: ConfigurableLocation = AtPlayerLocation(),
    private val power: Float = 4F,
    private val setFire: Boolean = false,
    private val breakBlocks: Boolean = false,
    private val fuseTicks: Int = 0
) : GearyAction() {
    private val GearyEntity.location by at

    override fun GearyEntity.run(): Boolean {
        if (fuseTicks <= 0)
            location.createExplosion(power, setFire, breakBlocks)
        else //only spawn a tnt in if we have a fuse
            location.spawn<TNTPrimed> {
                fuseTicks = this@Explode.fuseTicks
            }
        return true
    }
}

