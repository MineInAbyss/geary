package com.mineinabyss.geary.minecraft.actions

import com.mineinabyss.geary.ecs.api.actions.GearyAction
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.entities.parent
import com.mineinabyss.geary.minecraft.properties.AtPlayerLocation
import com.mineinabyss.geary.minecraft.properties.ConfigurableLocation
import com.mineinabyss.idofront.messaging.broadcastVal
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.WeatherType
import org.bukkit.entity.Player
import org.bukkit.event.weather.WeatherEvent


/**
 * Spawns a fireball above a given location which flies down towards it.
 *
 * @param at The location to change the weather in.
 */
@Serializable
@SerialName("geary:toggle_storm")
public class ToggleStormAction(
    private val at: ConfigurableLocation,
) : GearyAction() {
    private val GearyEntity.location by at

    override fun GearyEntity.run(): Boolean {
        val world = location.world
        world.setStorm(!world.hasStorm())
        return true
    }
}
