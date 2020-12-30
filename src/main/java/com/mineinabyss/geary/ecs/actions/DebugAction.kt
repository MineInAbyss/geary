package com.mineinabyss.geary.ecs.actions

import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.idofront.messaging.broadcast
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * An action that will broadcast a debug [msg] when run.
 *
 * @param msg The message to be broadcast.
 */
@Serializable
@SerialName("debug")
public class DebugAction(
        private val msg: String
) : GearyAction() {
    override fun runOn(entity: GearyEntity): Boolean {
        broadcast(msg)
        return true
    }
}
