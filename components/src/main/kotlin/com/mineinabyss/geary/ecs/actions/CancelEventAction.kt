package com.mineinabyss.geary.ecs.actions

import com.mineinabyss.geary.ecs.api.actions.GearyAction
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * An action that cancels the Bukkit event that caused it to run. Always returns true as it is handled elsewhere.
 */
@Serializable
@SerialName("cancel")
public class CancelEventAction : GearyAction() {
    override fun runOn(entity: GearyEntity): Boolean {
        return true
    }
}
