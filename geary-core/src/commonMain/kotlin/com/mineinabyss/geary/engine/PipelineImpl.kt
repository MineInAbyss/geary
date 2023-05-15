package com.mineinabyss.geary.engine

import com.mineinabyss.geary.addons.GearyPhase
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.Listener
import com.mineinabyss.geary.systems.RepeatingSystem
import com.mineinabyss.geary.systems.System
import com.soywiz.kds.sortedMapOf

class PipelineImpl : Pipeline {
    private val queryManager get() = geary.queryManager

    private val onSystemRegister = mutableListOf<(System) -> System?>()
    private val registeredSystems: MutableSet<RepeatingSystem> = mutableSetOf()
    private val registeredListeners: MutableSet<Listener> = mutableSetOf()

    private val actions = sortedMapOf<GearyPhase, MutableList<() -> Unit>>()

    override fun intercept(phase: GearyPhase, block: () -> Unit) {
        actions.getOrPut(phase) { mutableListOf() }.add(block)
    }

    override fun interceptSystemAddition(run: (System) -> System?) {
        onSystemRegister.add(run)
    }

    override fun runStartupTasks() {
        actions.values.forEach { actions ->
            actions.forEach { it() }
        }
    }
    override fun addSystem(system: System) {
        val resultSystem = onSystemRegister.fold(system) { acc, func -> func(acc) ?: return }
        // Track systems right at startup since they are likely going to tick very soon anyway, and we don't care about
        // any hiccups at that point.
        when (resultSystem) {
            is RepeatingSystem -> {
                if (resultSystem in registeredSystems) return
                queryManager.trackQuery(resultSystem)
                registeredSystems.add(resultSystem)
            }

            is Listener -> {
                if (resultSystem in registeredListeners) return
                resultSystem.start()
                queryManager.trackEventListener(resultSystem)
                registeredListeners.add(resultSystem)
            }

            else -> resultSystem.onStart()
        }
    }

    override fun addSystems(vararg systems: System) {
        systems.forEach { addSystem(it) }
    }

    override fun getRepeatingInExecutionOrder(): Iterable<RepeatingSystem> {
        return registeredSystems
    }
}
