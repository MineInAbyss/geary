package com.mineinabyss.geary.minecraft

import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.engine.countChildren
import com.mineinabyss.geary.ecs.engine.getArchetype
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.PrefabManager
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import com.mineinabyss.idofront.commands.execution.stopCommand
import com.mineinabyss.idofront.messaging.info
import com.rylinaux.plugman.util.PluginUtil
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

internal class GearyCommands : IdofrontCommandExecutor(), TabCompleter {
    override val commands = commands(GearyPlugin.instance) {
        "geary" {
            "reread" {
                val prefab by stringArg()
                action {
                    PrefabManager.reread(
                        PrefabKey.ofOrNull(prefab)?.toEntity() ?: command.stopCommand("Prefab key not found")
                    )
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
            "countArchetypes" {
                action {
                    sender.info("${GearyType().getArchetype().countChildren()} archetypes registered.")
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

    override fun onTabComplete(
        sender: CommandSender,
        command: org.bukkit.command.Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        return when (args.size) {
            1 -> listOf(
                "reread",
                "fullreload",
                "countArchetypes"
            )
            2 -> {
                when (args[0]) {
                    "reread" -> PrefabManager.keys.filter {
                        val arg = args[1].lowercase()
                        it.name.startsWith(arg) || it.key.startsWith(arg)
                    }.map { it.toString() }
                    else -> listOf()
                }
            }
            else -> listOf()
        }
    }
}
