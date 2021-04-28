package com.mineinabyss.geary.minecraft.actions.context

import com.mineinabyss.geary.ecs.api.actions.GearyAction
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.components.Source
import com.mineinabyss.geary.minecraft.access.geary
import com.mineinabyss.geary.minecraft.properties.AtEntityLocation
import com.mineinabyss.geary.minecraft.properties.ConfigurableLocation
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Entity

/**
 * Executes actions on nearby entities.
 *
 * @param radius Will run on entities in a cube box extending [radius] blocks into each direction.
 * @param max The maximum number of entities to select. Will sort by distance.
 * @param at The location from which to start the search.
 * @param onSelf Whether to include self within the found entities.
 * @param run The actions to run on all entities found.
 */
@Serializable
@SerialName("on.nearby")
public class OnNearbyAction(
    public val radius: Double,
    public val max: Int? = null,
    public val at: ConfigurableLocation = AtEntityLocation(),
    public val onSelf: Boolean = false,
    public val run: List<GearyAction>,
) : GearyAction() {
    private val GearyEntity.loc by at
    private val GearyEntity.bukkitEntity by get<Entity>()

    override fun GearyEntity.run(): Boolean {
        return loc.getNearbyEntities(radius, radius, radius)
            .run {
                // Ignore self if asked to
                if (!onSelf) remove(bukkitEntity)

                // Take only the max number of entities from the list
                if (max != null) sortedBy { loc.distanceSquared(it.location) }.take(max)
                else this
            }
            .map { geary(it) }
            .count { target ->
                target.set(Source(this))
                run.count { action -> action.runOn(target) } != 0
                target.remove<Source>()
            } != 0
    }
}
