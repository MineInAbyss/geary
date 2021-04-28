package com.mineinabyss.geary.ecs.actions

import com.mineinabyss.geary.ecs.api.actions.GearyAction
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.idofront.messaging.broadcast
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * > debug
 *
 * An action that will broadcast a debug [msg] when run.
 *
 * @param msg The message to be broadcast.
 */
@Serializable
@SerialName("debug")
public class DebugAction(
    private val msg: String
) : GearyAction() {
    override fun GearyEntity.run(): Boolean {
        broadcast(msg)
        return true
    }
}
