package com.mineinabyss.geary.papermc.plugin

import com.mineinabyss.geary.helpers.countChildren
import com.mineinabyss.geary.papermc.GearyMCContext
import com.mineinabyss.geary.papermc.GearyMCContextKoin
import com.mineinabyss.geary.papermc.StartupEventListener
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import com.mineinabyss.idofront.commands.execution.stopCommand
import com.mineinabyss.idofront.messaging.info
import com.rylinaux.plugman.util.PluginUtil
import kotlinx.coroutines.launch
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

internal class GearyCommands : IdofrontCommandExecutor(), TabCompleter, GearyMCContext by GearyMCContextKoin() {
    override val commands = commands(geary) {
        "geary" {
            "reread" {
                val prefab by stringArg()
                action {
                    engine.launch {
                        prefabManager.reread(
                            PrefabKey.ofOrNull(prefab)?.toEntity() ?: command.stopCommand("Prefab key not found")
                        )
                    }
                }
            }
            "fullreload" {
                action {
                    val depends = StartupEventListener.getGearyDependants()
                    depends.forEach { PluginUtil.unload(it) }
                    PluginUtil.reload(geary)
                    depends.forEach { PluginUtil.load(it.name) }
                }
            }
            "countArchetypes" {
                action {
                    sender.info("${engine.rootArchetype.countChildren()} archetypes registered.")
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
                    "reread" -> prefabManager.keys.filter {
                        val arg = args[1].lowercase()
                        it.key.startsWith(arg) || it.full.startsWith(arg)
                    }.map { it.toString() }
                    else -> listOf()
                }
            }
            else -> listOf()
        }
    }
}
