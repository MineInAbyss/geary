package com.mineinabyss.geary.papermc.plugin

import com.mineinabyss.geary.papermc.GearyMCContext
import com.mineinabyss.geary.papermc.GearyMCContextKoin
import com.mineinabyss.geary.papermc.GearyPlugin
import com.mineinabyss.geary.papermc.StartupEventListener
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.PrefabManager
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import com.mineinabyss.idofront.messaging.*
import com.rylinaux.plugman.util.PluginUtil
import kotlinx.coroutines.launch
import okio.Path.Companion.toPath
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import java.io.File
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries

internal class GearyCommands : IdofrontCommandExecutor(), TabCompleter, GearyMCContext by GearyMCContextKoin() {
    override val commands = commands(geary) {
        "geary" {
            "load" {
                val prefab by stringArg()
                action {
                    val prefabKey = PrefabKey.of(prefab)
                    val file: File? = gearyEntityFiles.firstOrNull { it.name == "${prefabKey.key}.yml" }

                    if (file == null) {
                        sender.error("Could not load <i>$prefab</i>, as no file could be tied to this entity")
                        return@action
                    }

                    if (prefabKey.toEntityOrNull() != null) {
                        sender.error("<i>$prefab</i> has already been loaded")
                        sender.warn("Use /geary reread if you wish to reload it")
                        return@action
                    }

                    prefabManager.loadFromFile(prefabKey.namespace, file)
                    // If entity is null it means it failed to load, and we should skip it
                    if (prefabKey.toEntityOrNull() == null) sender.error("Failed to load <i>$prefab</i> into a GearyEntity")
                    else sender.success("Loaded <i>$prefab</i> into a GearyEntity")
                }
            }
            "reread" {
                val prefab by stringArg()
                action {
                    engine.launch {
                        prefabManager.reread(PrefabKey.of(prefab).toEntity())
                    }
                    sender.success("Reloaded $prefab GearyEntity")
                }
            }
            "fullreload" {
                action {
                    val depends = StartupEventListener.getGearyDependants()
                    depends.forEach { PluginUtil.unload(it) }
                    PluginUtil.reload(geary)
                    depends.forEach { PluginUtil.load(it.name) }
                    sender.success("Reloaded all GearyEntities")
                }
            }
//            "countArchetypes" {
//                action {
//                    sender.info("${(engine).rootArchetype.countChildren()} archetypes registered.")
//                }
//            }
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

    private val gearyEntityFiles = geary.dataFolder.walkTopDown().filter { it.name != "config.yml" }.toMutableList()
    override fun onTabComplete(
        sender: CommandSender,
        command: org.bukkit.command.Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        return when (args.size) {
            1 -> listOf(
                "load",
                "reread",
                "fullreload",
                "countArchetypes"
            )

            2 -> {
                when (args[0]) {
                    "load" -> gearyEntityFiles.filter {
                        val arg = args[1].lowercase()
                        val prefab = "${it.getGearyNamespace()}:${it.nameWithoutExtension}"
                        (it.nameWithoutExtension.startsWith(arg) || prefab.startsWith(arg)) &&
                        PrefabKey.of(prefab).toEntityOrNull() == null
                    }.map { "${it.getGearyNamespace()}:${it.nameWithoutExtension}" }.toList()
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

    private fun File.getGearyNamespace() = this.path.replaceFirst(geary.dataFolder.path + "\\", "").substringBefore("\\")
}
