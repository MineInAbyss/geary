package com.mineinabyss.geary.minecraft

import com.mineinabyss.idofront.commands.execution.ExperimentalCommandDSL
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor

@ExperimentalCommandDSL
internal class GearyCommands : IdofrontCommandExecutor() {
    override val commands = commands(geary) {
        "geary" {
            //TODO reimplement
            /*"components"{
                val type by stringArg()
                action {
                    val count = (getService<Engine>() as GearyEngine).countEntitiesOfType(type)

                    sender.info("$count entities with that component")
                }
            }*/
        }
    }
}
