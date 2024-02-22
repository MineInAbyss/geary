package com.mineinabyss.geary.engine

import com.mineinabyss.geary.addons.GearyPhase
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.Listener
import com.mineinabyss.geary.systems.System
import com.mineinabyss.geary.systems.TrackedSystem
import com.mineinabyss.geary.systems.query.CachedQueryRunner
import kotlin.time.Duration

class PipelineImpl : Pipeline {
    private val queryManager get() = geary.queryManager

    private val onSystemRegister = mutableListOf<(System) -> System?>()
    private val registeredSystems: MutableSet<TrackedSystem> = mutableSetOf()
    private val registeredListeners: MutableSet<Listener<*>> = mutableSetOf()

    private val scheduled = Array(GearyPhase.entries.size) { mutableListOf<() -> Unit>() }
    private var currentPhase = GearyPhase.entries.first()

    override fun runOnOrAfter(phase: GearyPhase, block: () -> Unit) {
        if (currentPhase > phase) block()
        else scheduled[phase.ordinal].add(block)
    }

    override fun interceptSystemAddition(run: (System) -> System?) {
        onSystemRegister.add(run)
    }

    override fun runStartupTasks() {
        scheduled.forEach { actions ->
            actions.forEach { it() }
        }
    }

    override fun addSystem(system: System): TrackedSystem {
//        val resultSystem = onSystemRegister.fold(system) { acc, func -> func(acc) ?: return }
        // Track systems right at startup since they are likely going to tick very soon anyway, and we don't care about
        // any hiccups at that point.
        val runner = queryManager.trackQuery(system.query)
        val tracked = TrackedSystem(system, runner)
        if (system.interval != Duration.ZERO) {
            registeredSystems.add(tracked)
        }
        return TrackedSystem(system, runner)
//        when (resultSystem) {
//            else -> resultSystem.onStart()
//        }
    }

    override fun addSystems(vararg systems: System) {
        systems.forEach { addSystem(it) }
    }

    override fun addListener(listener: Listener<*>): Listener<*> {
        if (listener in registeredListeners) return listener
        queryManager.trackEventListener(listener)
        registeredListeners.add(listener)
        return listener
    }

    override fun getRepeatingInExecutionOrder(): Iterable<TrackedSystem> {
        return registeredSystems
    }
}
