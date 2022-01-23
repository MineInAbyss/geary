package com.mineinabyss.geary.minecraft.dsl

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.serialization.Formats
import com.mineinabyss.geary.minecraft.GearyPlugin
import com.mineinabyss.geary.minecraft.events.GearyPrefabLoadEvent
import com.mineinabyss.idofront.events.call
import com.okkero.skedule.schedule
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

public object GearyLoadManager: KoinComponent {
    private val plugin by inject<GearyPlugin>()
    internal val loadingPrefabs = mutableListOf<GearyEntity>()
    private val actions = sortedMapOf<GearyLoadPhase, MutableList<() -> Unit>>()

    public fun add(phase: GearyLoadPhase, action: () -> Unit) {
        if (actions.isEmpty()) scheduleLoadTasks()

        actions.getOrPut(phase) { mutableListOf() }.add(action)
    }

    private fun MutableList<() -> Unit>.runAll() = forEach { it() }

    private fun scheduleLoadTasks() {
        plugin.schedule {
            waitFor(1)
            plugin.logger.info("Registering Serializers")
            actions[GearyLoadPhase.REGISTER_SERIALIZERS]?.runAll()
            Formats.createFormats()
            plugin.logger.info("Loading prefabs")
            actions[GearyLoadPhase.LOAD_PREFABS]?.runAll()
            loadingPrefabs.forEach {
                GearyPrefabLoadEvent(it).call()
            }
            loadingPrefabs.clear()

            waitFor(1)
            plugin.logger.info("Running final startup tasks")
            actions[GearyLoadPhase.ENABLE]?.runAll()
            actions.clear()
        }
    }
}
