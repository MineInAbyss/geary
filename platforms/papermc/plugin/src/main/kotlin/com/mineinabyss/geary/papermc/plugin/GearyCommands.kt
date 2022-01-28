package com.mineinabyss.geary.papermc.plugin

import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.engine.countChildren
import com.mineinabyss.geary.ecs.engine.getArchetype
import com.mineinabyss.geary.papermc.StartupEventListener
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.PrefabManager
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import com.mineinabyss.idofront.commands.execution.stopCommand
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.spawning.spawn
import com.rylinaux.plugman.util.PluginUtil
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.ArmorStand
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal class GearyCommands(val plugin: JavaPlugin) : IdofrontCommandExecutor(), TabCompleter, KoinComponent {
    val prefabManager: PrefabManager by inject()

    override val commands = commands(plugin) {
        "geary" {
            "reread" {
                val prefab by stringArg()
                action {
                    prefabManager.reread(
                        PrefabKey.ofOrNull(prefab)?.toEntity() ?: command.stopCommand("Prefab key not found")
                    )
                }
            }
            "fullreload" {
                action {
                    val depends = StartupEventListener.getGearyDependants()
                    depends.forEach { PluginUtil.unload(it) }
                    PluginUtil.reload(plugin)
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
                    "reread" -> prefabManager.keys.filter {
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
