package com.mineinabyss.geary.minecraft.actions

import com.mineinabyss.geary.ecs.api.actions.GearyAction
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.minecraft.properties.AtEntityLocation
import com.mineinabyss.geary.minecraft.properties.ConfigurableLocation
import com.mineinabyss.idofront.util.FloatRange
import com.mineinabyss.idofront.util.randomOrMin
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.SoundCategory
import org.bukkit.World

/**
 * Plays a sound.
 *
 * @param sound The name of the sound to play. Can be a custom sound if specified in `sounds.json`.
 * @param volume The volume of the sound.
 * @param pitch The pitch of the sound.
 * @param category The category of the sound.
 * @param at The location to play the sound at.
 *
 * @see World.spawnParticle
 */
@Serializable
@SerialName("geary:sound")
public data class PlaySoundAction(
    val sound: String,
    val volume: FloatRange = 1.0f..1.0f,
    val pitch: FloatRange = 0.8f..1.2f,
    val category: SoundCategory = SoundCategory.NEUTRAL,
    val at: ConfigurableLocation = AtEntityLocation(),
) : GearyAction() {
    private val GearyEntity.loc by at

    override fun GearyEntity.run(): Boolean {
        loc.world.playSound(loc, sound, category, volume.randomOrMin(), pitch.randomOrMin())
        return true
    }
}
