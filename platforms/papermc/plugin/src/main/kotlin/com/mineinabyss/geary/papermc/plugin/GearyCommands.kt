package com.mineinabyss.geary.papermc.plugin

import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.modules.prefabs
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.rylinaux.plugman.util.PluginUtil
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.plugin.Plugin
import kotlin.io.path.Path
import kotlin.io.path.nameWithoutExtension

internal class GearyCommands : IdofrontCommandExecutor(), TabCompleter {
    private val plugin get() = gearyPaper.plugin
    private val prefabManager get() = prefabs.manager
    private val engine get() = geary.engine

    override val commands = commands(plugin) {
        "geary" {
            "reread" {
                val prefab by stringArg()
                action {
                    engine.launch {
                        runCatching { prefabManager.reread(PrefabKey.of(prefab).toEntity()) }
                            .onSuccess { sender.success("Reread prefab $prefab") }
                            .onFailure { sender.error("Failed to reread prefab $prefab:\n${it.message}") }
                    }
                }
            }
            "read" {
                val namespace by stringArg()
                val path by stringArg()
                action {
                    engine.launch {
                        // Ensure not already registered
                        if (prefabManager[PrefabKey.of(namespace, Path(path).nameWithoutExtension)] != null) {
                            sender.error("Prefab $namespace:$path already exists")
                            return@launch
                        }

                        // Try to load from file
                        runCatching {
                            prefabManager.loadFromFile(namespace, plugin.dataFolder.resolve(namespace).resolve(path))
                        }
                            .onSuccess { sender.success("Read prefab $namespace:$path") }
                            .onFailure { sender.error("Failed to read prefab $namespace:$path:\n${it.message}") }
                    }
                }
            }
            "fullreload" {
                action {
                    val depends = getGearyDependants()
                    depends.forEach { PluginUtil.unload(it) }
                    PluginUtil.reload(plugin)
                    depends.forEach { PluginUtil.load(it.name) }
                }
            }
        }
    }

    private fun getGearyDependants(): List<Plugin> =
        Bukkit.getServer().pluginManager.plugins.filter { "Geary" in it.description.depend }

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
