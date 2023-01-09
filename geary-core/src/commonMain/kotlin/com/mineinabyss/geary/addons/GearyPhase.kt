package com.mineinabyss.geary.addons

/**
 * Different phases of Geary's startup process.
 *
 * They are executed top to bottom.
 */
enum class GearyPhase {
    /** All addons have been installed and configured. */
    ADDONS_CONFIGURED,

    /** Initialize new components and any information related to them. */
    INIT_COMPONENTS,

    /** Initialize any systems before entities start being added to the world. */
    INIT_SYSTEMS,

    /** Create any entities that should exist before the engine starts running. */
    INIT_ENTITIES,

    /** All previous registration tasks have been completed. */
    ENABLE,
}
