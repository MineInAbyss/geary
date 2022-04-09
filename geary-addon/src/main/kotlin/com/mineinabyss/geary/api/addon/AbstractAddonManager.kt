package com.mineinabyss.geary.api.addon

import com.mineinabyss.geary.ecs.api.FormatsContext
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.prefabs.events.PrefabLoaded
import com.mineinabyss.idofront.messaging.logInfo

public abstract class AbstractAddonManager: FormatsContext {
    internal val loadingPrefabs = mutableListOf<GearyEntity>()
    private val actions = sortedMapOf<GearyLoadPhase, MutableList<suspend () -> Unit>>()

    public fun add(phase: GearyLoadPhase, action: suspend () -> Unit) {
        if (actions.isEmpty()) scheduleLoadTasks()

        actions.getOrPut(phase) { mutableListOf() }.add(action)
    }

    protected abstract fun scheduleLoadTasks()

    private suspend fun MutableList<suspend () -> Unit>.runAll() = forEach { it() }

    /** Tasks to run before all other addon startup tasks execute. */
    public suspend fun load() {
        logInfo("Registering Serializers")
        actions[GearyLoadPhase.REGISTER_SERIALIZERS]?.runAll()
        formats.createFormats()
        logInfo("Loading prefabs")
        actions[GearyLoadPhase.LOAD_PREFABS]?.runAll()
        loadingPrefabs.forEach {
            it.callEvent(PrefabLoaded())
        }
        loadingPrefabs.clear()
    }

    /** Run addons startup tasks. */
    public suspend fun enableAddons() {
        logInfo("Running final startup tasks")
        actions[GearyLoadPhase.ENABLE]?.runAll()
        actions.clear()
    }
}
