package com.mineinabyss.geary.ecs.actions

import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.components.CooldownManager
import com.mineinabyss.geary.ecs.components.getOrAddPersisting
import com.mineinabyss.idofront.time.TimeSpan
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * An action that will start a cooldown, storing it in the entity's [CooldownManager] component.
 *
 * The action will only succeed once the cooldown is over, which will then run a list of actions. If none of the
 * child actions succeed, the cooldown is not started.
 *
 * @param length The length of this cooldown.
 * @param run A list of actions to run when the cooldown is over.
 * @param _name The name of this cooldown, will be used as the key on this entity's [CooldownManager].
 * Defaults to the hashCode of [run].
 */
@Serializable
@SerialName("cooldown")
public class CooldownAction(
        private val length: TimeSpan,
        private val run: List<GearyAction>,
        @SerialName("name")
        public val _name: String? = null
) : GearyAction() {
    @Transient
    private val name = _name ?: run.hashCode().toString()

    override fun runOn(entity: GearyEntity): Boolean {
        val cooldowns = entity.getOrAddPersisting { CooldownManager() }

        // restart cooldown if any of the actions ran successfully
        //TODO maybe it's worth storing under hashCode but having a separate field for display name
        return cooldowns.onCooldownIf(name, length.inMillis) {
            run.count { it.runOn(entity) } != 0
        }
    }
}
