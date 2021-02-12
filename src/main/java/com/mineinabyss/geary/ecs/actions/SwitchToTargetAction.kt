package com.mineinabyss.geary.ecs.actions

import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.components.Target
import com.mineinabyss.geary.ecs.components.get
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Used to execute actions on a different [GearyEntity].
 *
 * @param run The actions to run on the other [GearyEntity].
 */
@Serializable
@SerialName("on.target")
public class SwitchToTargetAction(
    public val run: List<GearyAction>
) : GearyAction() {
    override fun runOn(entity: GearyEntity): Boolean {
        val target = entity.get<Target>()?.entity ?: return false

        return run.count{it.runOn(target)} != 0
    }
}