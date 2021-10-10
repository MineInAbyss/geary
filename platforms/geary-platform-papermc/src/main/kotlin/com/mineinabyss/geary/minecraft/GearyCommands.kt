package com.mineinabyss.geary.minecraft

import com.mineinabyss.geary.ecs.prefab.PrefabKey
import com.mineinabyss.geary.ecs.prefab.PrefabManager
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.execution.ExperimentalCommandDSL
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import com.rylinaux.plugman.util.PluginUtil

@ExperimentalCommandDSL
internal class GearyCommands : IdofrontCommandExecutor() {
    override val commands = commands(GearyPlugin.instance) {
        "geary" {
            "reread" {
                val prefab by stringArg()
                action {
                    PrefabManager.reread(PrefabKey.of(prefab).toEntity()!!)
                }
            }
            "fullreload" {
                action {
                    val depends = StartupEventListener.getGearyDependants()
                    depends.forEach { PluginUtil.unload(it) }
                    PluginUtil.reload(GearyPlugin.instance)
                    depends.forEach { PluginUtil.load(it.name) }
                }
            }
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
