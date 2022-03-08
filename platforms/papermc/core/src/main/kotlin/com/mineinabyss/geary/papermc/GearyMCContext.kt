package com.mineinabyss.geary.papermc

import com.mineinabyss.geary.api.addon.AbstractAddonManagerContext
import com.mineinabyss.geary.ecs.api.GearyContext
import com.mineinabyss.geary.ecs.entities.UUID2GearyMap
import com.mineinabyss.geary.ecs.helpers.GearyKoinComponent
import com.mineinabyss.geary.ecs.serialization.Formats
import com.mineinabyss.geary.papermc.access.BukkitEntity2Geary
import com.mineinabyss.geary.papermc.dsl.GearyAddonManager
import com.mineinabyss.geary.papermc.engine.PaperMCEngine
import com.mineinabyss.geary.prefabs.PrefabManager
import com.mineinabyss.geary.prefabs.PrefabManagerContext
import org.koin.core.component.inject

public interface BukkitEntityAssociationsContext {
    public val bukkit2Geary: BukkitEntity2Geary
}


public interface UUID2GearyContext {
    public val uuid2entity: UUID2GearyMap
}

public interface PluginContext {
    public val geary: GearyPlugin
}

public open class GearyMCContext :
    GearyKoinComponent(),
    GearyContext,
    PrefabManagerContext,
    AbstractAddonManagerContext,
    BukkitEntityAssociationsContext,
    UUID2GearyContext,
    PluginContext {
    override val engine: PaperMCEngine get() = super.engine as PaperMCEngine
    override val prefabManager: PrefabManager by inject()
    override val addonManager: GearyAddonManager by inject()
    public override val bukkit2Geary: BukkitEntity2Geary by inject()
    override val formats: Formats by inject()


    public override val geary: GearyPlugin by inject()

    public override val uuid2entity: UUID2GearyMap by inject()

    public companion object {
//        @Deprecated("Being replaced with context receivers")
//        public inline operator fun <T> invoke(run: GearyMCContext.() -> T): T {
//            return GearyMCContext().run(run)
//        }
    }
}
