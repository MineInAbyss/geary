package com.mineinabyss.geary.minecraft.dsl

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.minecraft.GearyPlugin
import com.mineinabyss.geary.minecraft.events.GearyPrefabLoadEvent
import com.mineinabyss.idofront.events.call
import com.okkero.skedule.schedule

public object GearyLoadManager {
    internal val loadingPrefabs = mutableListOf<GearyEntity>()
    private val actions = sortedMapOf<GearyLoadPhase, MutableList<() -> Unit>>()

    public fun add(phase: GearyLoadPhase, action: () -> Unit) {
        if (actions.isEmpty()) scheduleLoadTasks()

        actions.getOrPut(phase) { mutableListOf() }.add(action)
    }

    private fun MutableList<() -> Unit>.runAll() = forEach { it() }

    private fun scheduleLoadTasks() {
        GearyPlugin.instance.schedule {
            waitFor(1)
            GearyPlugin.instance.logger.info("Registering Serializers")
            actions[GearyLoadPhase.REGISTER_SERIALIZERS]?.runAll()
            GearyPlugin.instance.logger.info("Loading prefabs")
            actions[GearyLoadPhase.LOAD_PREFABS]?.runAll()
            loadingPrefabs.forEach {
                GearyPrefabLoadEvent(it).call()
            }
            loadingPrefabs.clear()

            waitFor(1)
            GearyPlugin.instance.logger.info("Running final startup tasks")
            actions[GearyLoadPhase.ENABLE]?.runAll()
            actions.clear()
        }
    }
}
