package com.mineinabyss.geary.papermc

import com.mineinabyss.geary.addon.GearyAddonManager
import com.mineinabyss.geary.addon.GearyAddonManagerContext
import com.mineinabyss.geary.context.EngineContext
import com.mineinabyss.geary.context.GearyArchetypeModule
import com.mineinabyss.geary.context.GearyContext
import com.mineinabyss.geary.datatypes.maps.UUID2GearyMap
import com.mineinabyss.geary.engine.SystemProvider
import com.mineinabyss.geary.engine.impl.UnorderedSystemProvider
import com.mineinabyss.geary.papermc.access.BukkitEntity2Geary
import com.mineinabyss.geary.papermc.engine.PaperMCEngine
import com.mineinabyss.geary.papermc.engine.PaperSystemProvider
import com.mineinabyss.geary.prefabs.PrefabManager
import com.mineinabyss.geary.prefabs.PrefabManagerContext

interface BukkitEntityAssociationsContext {
    val bukkit2Geary: BukkitEntity2Geary
}


interface UUID2GearyContext {
    val uuid2entity: UUID2GearyMap
}

interface PluginContext {
    val geary: GearyPlugin
}

interface PaperEngineContext : EngineContext {
    override val engine: PaperMCEngine
}

interface GearyMCContext :
    GearyContext,
    PaperEngineContext,
    PrefabManagerContext,
    GearyAddonManagerContext,
    BukkitEntityAssociationsContext,
    UUID2GearyContext,
    PluginContext {
}

open class GearyPaperModule(
    val plugin: GearyPlugin,
) : GearyArchetypeModule() {
    override val engine: PaperMCEngine = PaperMCEngine(plugin)
    val prefabManager: PrefabManager = PrefabManager(formats)
    val addonManager: GearyAddonManager = GearyAddonManager()
    val bukkit2Geary: BukkitEntity2Geary = BukkitEntity2Geary()
    val uuid2entity: UUID2GearyMap = UUID2GearyMap()
    override val systems: SystemProvider = PaperSystemProvider(plugin, UnorderedSystemProvider())
}

var gearyPaper: GearyPaperModule = TODO()
