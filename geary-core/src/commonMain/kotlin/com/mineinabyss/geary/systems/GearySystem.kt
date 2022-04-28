package com.mineinabyss.geary.systems

/**
 * An interface representing all types of systems that can be registered with the engine.
 * Includes [GearyListener] and [TickingSystem].
 */
public interface GearySystem {
    public fun onStart() {}
}
