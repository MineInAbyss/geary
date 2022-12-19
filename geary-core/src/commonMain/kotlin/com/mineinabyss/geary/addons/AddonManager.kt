package com.mineinabyss.geary.addons

import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.prefabs.events.PrefabLoaded

open class AddonManager {
    private val logger get() = geary.logger

    //    internal val loadingPrefabs = mutableListOf<Entity>()
    private val actions = sortedMapOf<GearyLoadPhase, MutableList<() -> Unit>>()

    fun add(phase: GearyLoadPhase, action: () -> Unit) {
        if (actions.isEmpty()) scheduleLoadTasks()

        actions.getOrPut(phase) { mutableListOf() }.add(action)
    }

    protected open fun scheduleLoadTasks() {
        load()
        enableAddons()
    }

    private fun MutableList<() -> Unit>.runAll() = forEach { it() }

    /** Tasks to run before all other addon startup tasks execute. */
    fun load() {
        logger.info("Registering Serializers")
        actions[GearyLoadPhase.REGISTER_SERIALIZERS]?.runAll()

        logger.info("Registering Formats")
        actions[GearyLoadPhase.REGISTER_FORMATS]?.runAll()

        logger.info("Loading prefabs")
        actions[GearyLoadPhase.LOAD_PREFABS]?.runAll()
        loadingPrefabs.forEach {
            it.callEvent(PrefabLoaded())
        }
        loadingPrefabs.clear()
    }

    /** Run addons startup tasks. */
    fun enableAddons() {
        logger.info("Running final startup tasks")
        actions[GearyLoadPhase.ENABLE]?.runAll()
        actions.clear()
    }
}
