package com.mineinabyss.geary.engine.impl

import com.mineinabyss.geary.context.archetypes
import com.mineinabyss.geary.engine.SystemProvider
import com.mineinabyss.geary.systems.Listener
import com.mineinabyss.geary.systems.RepeatingSystem
import com.mineinabyss.geary.systems.System

/**
 * Runs systems in no particular order.
 */
public class UnorderedSystemProvider : SystemProvider {
    private val queryManager get() = archetypes.queryManager

    private val registeredSystems: MutableSet<RepeatingSystem> = mutableSetOf()
    private val registeredListeners: MutableSet<Listener> = mutableSetOf()

    override fun add(system: System) {
        // Track systems right at startup since they are likely going to tick very soon anyways and we don't care about
        // any hiccups at that point.
        when (system) {
            is RepeatingSystem -> {
                if (system in registeredSystems) return
                queryManager.trackQuery(system)
                registeredSystems.add(system)
            }

            is Listener -> {
                if (system in registeredListeners) return
                system.start()
                queryManager.trackEventListener(system)
                registeredListeners.add(system)
            }

            else -> system.onStart()
        }
    }

    override fun getRepeatingInExecutionOrder(): Iterable<RepeatingSystem> {
        return registeredSystems
    }
}
