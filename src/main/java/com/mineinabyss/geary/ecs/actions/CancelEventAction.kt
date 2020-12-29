package com.mineinabyss.geary.ecs.actions

import com.mineinabyss.geary.ecs.GearyEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("cancel")
public class CancelEventAction : GearyAction() {
    override fun runOn(entity: GearyEntity): Boolean {
        return true
    }
}
