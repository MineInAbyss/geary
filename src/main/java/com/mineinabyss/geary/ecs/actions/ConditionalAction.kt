package com.mineinabyss.geary.ecs.actions

import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.actions.components.ComponentAction
import com.mineinabyss.geary.ecs.components.get
import com.mineinabyss.geary.ecs.components.hasAll
import com.mineinabyss.geary.ecs.components.parent
import com.mineinabyss.geary.minecraft.components.PlayerComponent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

@Serializable
@SerialName("if")
public class ConditionalAction(
        @SerialName("has")
        override val components: Set<String> = emptySet(),
        private val player: PlayerConditions? = null,
        private val run: List<GearyAction>
) : ComponentAction() {
    override fun runOn(entity: GearyEntity) {
        if (entity.hasAll(componentClasses) &&
                player?.conditionsMet(entity.parent?.get<PlayerComponent>()?.player ?: return) == true)
            run.forEach { it.runOn(entity) }
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
