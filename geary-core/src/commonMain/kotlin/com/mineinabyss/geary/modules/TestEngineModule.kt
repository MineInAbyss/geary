package com.mineinabyss.geary.modules

/**
 * An engine module that initializes the engine but does not start it, useful for testing.
 *
 * No pipeline tasks are run, and the engine won't be scheduled for ticking.
 * Engine ticks may still be called manually.
 */
// TODO set beginTickingOnStart = false property
val TestEngineModule get() = ArchetypeEngineModule(
    beginTickingOnStart = false
)
