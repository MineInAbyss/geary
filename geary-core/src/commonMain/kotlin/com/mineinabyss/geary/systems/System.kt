package com.mineinabyss.geary.systems

/**
 * An interface representing all types of systems that can be registered with the engine.
 * Includes [Listener] and [RepeatingSystem].
 */
interface System {
    fun onStart() {}
}
