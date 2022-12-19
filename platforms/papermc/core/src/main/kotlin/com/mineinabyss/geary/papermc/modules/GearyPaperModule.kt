package com.mineinabyss.geary.papermc.modules

import com.mineinabyss.geary.addon.modules.AddonModule
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.datatypes.maps.UUID2GearyMap
import com.mineinabyss.geary.papermc.GearyPlugin
import com.mineinabyss.geary.papermc.access.BukkitEntity2Geary
import com.mineinabyss.geary.papermc.engine.PaperMCEngine
import com.mineinabyss.geary.papermc.engine.PaperSystemProvider
import com.mineinabyss.idofront.di.DI

val gearyPaper: GearyPaperModule by DI.observe()

class GearyPaperModule(
    private val geary: GearyModule,
    private val addons: AddonModule,
    val plugin: GearyPlugin,
) : GearyModule by geary, AddonModule by addons {
    override val engine = PaperMCEngine()
    override val systems = PaperSystemProvider(plugin, geary.systems)

    val bukkit2Geary = BukkitEntity2Geary()
    val uuid2entity = UUID2GearyMap()

    override fun inject() {
        geary.inject()
        addons.inject()
        DI.add(gearyPaper)
    }

    fun start() {
        engine.start()
        uuid2entity.startTracking()
        bukkit2Geary.startTracking()
    }
}
