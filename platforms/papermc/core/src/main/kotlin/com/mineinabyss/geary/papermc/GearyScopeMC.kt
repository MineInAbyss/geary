package com.mineinabyss.geary.papermc

import com.mineinabyss.geary.api.addon.AbstractAddonManagerScope
import com.mineinabyss.geary.ecs.api.engine.EngineScope
import com.mineinabyss.geary.ecs.entities.UUID2GearyMap
import com.mineinabyss.geary.ecs.helpers.GearyKoinComponent
import com.mineinabyss.geary.papermc.access.BukkitEntity2Geary
import com.mineinabyss.geary.papermc.dsl.GearyAddonManager
import com.mineinabyss.geary.papermc.engine.PaperMCEngine
import com.mineinabyss.geary.prefabs.PrefabManager
import com.mineinabyss.geary.prefabs.PrefabManagerScope
import org.koin.core.component.inject

public interface GearyScope : EngineScope, PrefabManagerScope,
    AbstractAddonManagerScope, BukkitEntityAssociationsScope, PluginScope

public interface BukkitEntityAssociationsScope {
    public val bukkitEntity2Geary: BukkitEntity2Geary
}

public interface PluginScope {
    public val geary: GearyPlugin
}

public open class GearyScopeMC : GearyKoinComponent(), GearyScope {
    override val engine: PaperMCEngine get() = super.engine as PaperMCEngine
    override val prefabManager: PrefabManager by inject()
    override val addonManager: GearyAddonManager by inject()
    public override val bukkitEntity2Geary: BukkitEntity2Geary by inject()

    public override val geary: GearyPlugin by inject()
    public val bukkit2Geary: BukkitEntity2Geary by inject()

    public val uuid2entity: UUID2GearyMap by inject()

    public companion object {
        public inline operator fun <T> invoke(run: GearyScopeMC.() -> T): T {
            return GearyScopeMC().run(run)
        }
    }
}
