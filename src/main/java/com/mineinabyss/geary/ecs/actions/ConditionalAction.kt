package com.mineinabyss.geary.ecs.actions

import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.actions.components.toComponentClass
import com.mineinabyss.geary.ecs.components.get
import com.mineinabyss.geary.ecs.components.hasAll
import com.mineinabyss.geary.ecs.components.parent
import com.mineinabyss.geary.minecraft.components.PlayerComponent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

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

@Serializable
@SerialName("if")
public class ConditionalAction(
    private val condition: Condition,
    private val run: List<GearyAction>
) : GearyAction() {
    override fun runOn(entity: GearyEntity): Boolean {
        if (condition.conditionsMet(entity)) {
            run.forEach { it.runOn(entity) }
            return true
        }
        return false
    }
}

@Serializable
public class PlayerConditions(
    public val isSneaking: Boolean? = null,
    public val isSprinting: Boolean? = null,
) {
    private infix fun <T> T?.nullOrEquals(other: T?): Boolean =
        this == null || this == other

    public fun conditionsMet(player: Player): Boolean =
        isSneaking nullOrEquals player.isSneaking &&
                isSprinting nullOrEquals player.isSprinting
}
