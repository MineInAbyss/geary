package com.mineinabyss.geary.ecs.actions

import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.idofront.messaging.broadcast
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("debug")
public class DebugAction(
        private val msg: String
) : GearyAction() {
    override fun runOn(entity: GearyEntity) {
        broadcast(msg)
    }
}
