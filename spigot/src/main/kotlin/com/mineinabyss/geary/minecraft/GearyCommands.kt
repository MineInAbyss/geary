package com.mineinabyss.geary.minecraft

import com.mineinabyss.geary.ecs.engine.Engine
import com.mineinabyss.geary.ecs.engine.GearyEngine
import com.mineinabyss.geary.ecs.helpers.countEntitiesOfType
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.execution.ExperimentalCommandDSL
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.plugin.getService

@ExperimentalCommandDSL
internal object GearyCommands : IdofrontCommandExecutor() {
    override val commands = commands(geary) {
        "geary" {
            "components"{
                val type by stringArg()
                action {
                    val count = (getService<Engine>() as GearyEngine).countEntitiesOfType(type)

                    sender.info("$count entities with that component")
                }
            }
        }
    }
}
