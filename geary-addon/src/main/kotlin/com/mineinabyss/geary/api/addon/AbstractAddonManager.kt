package com.mineinabyss.geary.api.addon

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.serialization.Formats
import com.mineinabyss.geary.prefabs.events.PrefabLoaded
import com.mineinabyss.idofront.messaging.logInfo
import org.koin.core.component.KoinComponent

public abstract class AbstractAddonManager : KoinComponent {
    internal val loadingPrefabs = mutableListOf<GearyEntity>()
    private val actions = sortedMapOf<GearyLoadPhase, MutableList<() -> Unit>>()

    public fun add(phase: GearyLoadPhase, action: () -> Unit) {
        if (actions.isEmpty()) scheduleLoadTasks()

        actions.getOrPut(phase) { mutableListOf() }.add(action)
    }

    protected abstract fun scheduleLoadTasks()

    private fun MutableList<() -> Unit>.runAll() = forEach { it() }

    /** Tasks to run before all other addon startup tasks execute. */
    public fun load() {
        logInfo("Registering Serializers")
        actions[GearyLoadPhase.REGISTER_SERIALIZERS]?.runAll()
        Formats.createFormats()
        logInfo("Loading prefabs")
        actions[GearyLoadPhase.LOAD_PREFABS]?.runAll()
        loadingPrefabs.forEach {
            it.callEvent(PrefabLoaded())
        }
        loadingPrefabs.clear()
    }

    /** Run addons startup tasks. */
    public fun enableAddons() {
        logInfo("Running final startup tasks")
        actions[GearyLoadPhase.ENABLE]?.runAll()
        actions.clear()
    }
}
