package com.mineinabyss.geary.minecraft

import com.mineinabyss.geary.ecs.engine.Engine
import com.mineinabyss.geary.ecs.engine.GearyEngine
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.execution.ExperimentalCommandDSL
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.plugin.getService

@ExperimentalCommandDSL
internal object GearyCommands: IdofrontCommandExecutor() {
    override val commands = commands(minecraft.geary) {
        "geary" {
            "components"{
                val type by stringArg()
                action {
                    (getService<Engine>() as GearyEngine).bitsets.forEach { (t, u) ->
                        if (t.simpleName == type) {
                            var sum = 0
                            u.forEachBit { sum++ }
                            sender.info("$sum entities with that component")
                        }
                    }
                }
            }
        }
    }
}
