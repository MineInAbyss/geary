package com.mineinabyss.geary.engine

import com.mineinabyss.geary.addons.GearyPhase
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.Listener
import com.mineinabyss.geary.systems.System
import com.mineinabyss.geary.systems.TrackedSystem
import com.mineinabyss.geary.systems.query.Query

class PipelineImpl : Pipeline {
    private val queryManager get() = geary.queryManager

    private val onSystemAdd = mutableListOf<(System<*>) -> Unit>()
    private val repeatingSystems: MutableSet<TrackedSystem<*>> = mutableSetOf()
    private val registeredListeners: MutableSet<Listener<*>> = mutableSetOf()

    private val scheduled = Array(GearyPhase.entries.size) { mutableListOf<() -> Unit>() }
    private var currentPhase = GearyPhase.entries.first()

    override fun runOnOrAfter(phase: GearyPhase, block: () -> Unit) {
        if (currentPhase > phase) block()
        else scheduled[phase.ordinal].add(block)
    }

    override fun onSystemAdd(run: (System<*>) -> Unit) {
        onSystemAdd.add(run)
    }

    override fun runStartupTasks() {
        scheduled.forEach { actions ->
            actions.forEach { it() }
        }
    }

    override fun <T : Query> addSystem(system: System<T>): TrackedSystem<*> {
        onSystemAdd.forEach { it(system) }
        val runner = queryManager.trackQuery(system.query)
        val tracked = TrackedSystem(system, runner)
        if (system.interval != null) {
            repeatingSystems.add(tracked)
        }
        return TrackedSystem(system, runner)
    }

    override fun addSystems(vararg systems: System<*>) {
        systems.forEach { addSystem(it) }
    }

    override fun addListener(listener: Listener<*>): Listener<*> {
        if (listener in registeredListeners) return listener
        queryManager.trackEventListener(listener)
        registeredListeners.add(listener)
        return listener
    }

    override fun getRepeatingInExecutionOrder(): Iterable<TrackedSystem<*>> {
        return repeatingSystems
    }
}
