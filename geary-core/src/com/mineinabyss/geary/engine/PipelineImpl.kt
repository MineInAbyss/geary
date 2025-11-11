package com.mineinabyss.geary.engine

import androidx.collection.MutableObjectList
import com.mineinabyss.geary.addons.GearyPhase
import com.mineinabyss.geary.helpers.fastForEach
import com.mineinabyss.geary.systems.System
import com.mineinabyss.geary.systems.TrackedSystem
import com.mineinabyss.geary.systems.query.Query

class PipelineImpl(
    val queryManager: QueryManager,
) : Pipeline {
    private val onSystemAdd = MutableObjectList<(System<*>) -> Unit>()
    private val repeatingSystems: MutableSet<TrackedSystem<*>> = mutableSetOf()

    private val scheduled = Array(GearyPhase.entries.size) { MutableObjectList<() -> Unit>() }
    private var currentPhase = GearyPhase.entries.first()

    override fun runOnOrAfter(phase: GearyPhase, block: () -> Unit) {
        if (currentPhase > phase) block()
        else scheduled[phase.ordinal].add(block)
    }

    override fun onSystemAdd(run: (System<*>) -> Unit) {
        onSystemAdd.add(run)
    }

    override fun runStartupTasks() {
        scheduled.fastForEach { actions ->
            actions.forEach { it() }
        }
    }

    override fun <T : Query> addSystem(system: System<T>): TrackedSystem<*> {
        onSystemAdd.forEach { it(system) }
        val runner = queryManager.trackQuery(system.query)
        val tracked = TrackedSystem(system, runner)
        repeatingSystems.add(tracked)
        return tracked
    }

    override fun removeSystem(system: TrackedSystem<*>): Boolean {
        if (system !in repeatingSystems) return false
        repeatingSystems.remove(system)
        return queryManager.untrackQuery(system.runner)
    }

    override fun addSystems(vararg systems: System<*>) {
        systems.fastForEach { addSystem(it) }
    }

    override fun getRepeatingInExecutionOrder(): Iterable<TrackedSystem<*>> {
        return repeatingSystems
    }
}
