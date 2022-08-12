package com.mineinabyss.geary.papermc

import com.mineinabyss.geary.addon.GearyAddonManager
import com.mineinabyss.geary.addon.GearyAddonManagerContext
import com.mineinabyss.geary.context.EngineContext
import com.mineinabyss.geary.context.GearyContext
import com.mineinabyss.geary.context.GearyContextKoin
import com.mineinabyss.geary.datatypes.maps.UUID2GearyMap
import com.mineinabyss.geary.papermc.access.BukkitEntity2Geary
import com.mineinabyss.geary.papermc.engine.PaperMCEngine
import com.mineinabyss.geary.prefabs.PrefabManager
import com.mineinabyss.geary.prefabs.PrefabManagerContext
import org.koin.core.component.inject

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
    companion object {
        @Deprecated("Being replaced with context receivers")
        inline operator fun <T> invoke(run: GearyMCContext.() -> T): T {
            return GearyMCContextKoin().run(run)
        }
    }
}

open class GearyMCContextKoin :
    GearyContextKoin(),
    GearyMCContext {
    override val engine: PaperMCEngine get() = super.engine as PaperMCEngine
    override val prefabManager: PrefabManager by inject()
    override val addonManager: GearyAddonManager by inject()
    override val bukkit2Geary: BukkitEntity2Geary by inject()

    override val geary: GearyPlugin by inject()

    override val uuid2entity: UUID2GearyMap by inject()
}

var globalContextMC: GearyMCContext = GearyMCContextKoin()
