package com.mineinabyss.geary.ecs.actions.conditions

import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.actions.components.toComponentClass
import com.mineinabyss.geary.ecs.components.get
import com.mineinabyss.geary.ecs.components.hasAll
import com.mineinabyss.geary.ecs.components.parent
import com.mineinabyss.geary.minecraft.components.PlayerComponent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A serializable condition that can be checked against a certain entity.
 *
 * @param components Components the entity must have.
 * @param player Additional conditions relating to the player associated with this entity.
 */
@Serializable
public class Condition(
    @SerialName("has")
    public val components: Set<String> = emptySet(),
    public val player: PlayerConditions? = null,
) {
    //TODO getting boilerplatey, reused from ComponentAction
    private val componentClasses by lazy { components.map { it.toComponentClass() } }

    public fun conditionsMet(entity: GearyEntity): Boolean {
        return entity.hasAll(componentClasses) &&
                player?.conditionsMet(entity.parent?.get<PlayerComponent>()?.player ?: return false) != false
    }
}
