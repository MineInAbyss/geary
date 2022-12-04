package com.mineinabyss.geary.papermc

import com.mineinabyss.geary.addon.GearyAddonManager
import com.mineinabyss.geary.context.GearyModule
import com.mineinabyss.geary.datatypes.maps.UUID2GearyMap
import com.mineinabyss.geary.engine.SystemProvider
import com.mineinabyss.geary.engine.impl.UnorderedSystemProvider
import com.mineinabyss.geary.papermc.access.BukkitEntity2Geary
import com.mineinabyss.geary.papermc.engine.PaperMCEngine
import com.mineinabyss.geary.papermc.engine.PaperSystemProvider
import com.mineinabyss.geary.prefabs.PrefabManager


val gearyPaper: GearyPaperModule = TODO()

open class GearyPaperModule(
    val geary: GearyModule,
    val plugin: GearyPlugin,
) : GearyModule by geary {
    override val engine: PaperMCEngine = PaperMCEngine()
    val prefabManager: PrefabManager = PrefabManager()
    val addonManager: GearyAddonManager = GearyAddonManager()
    val bukkit2Geary: BukkitEntity2Geary = BukkitEntity2Geary()
    val uuid2entity: UUID2GearyMap = UUID2GearyMap()
    override val systems: SystemProvider = PaperSystemProvider(plugin, geary.systems)
}
