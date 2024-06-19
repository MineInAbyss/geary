package com.mineinabyss.geary.modules

import com.mineinabyss.geary.addons.GearyPhase
import com.mineinabyss.geary.addons.Namespaced

fun GearyModule.namespace(namespace: String, configure: Namespaced.() -> Unit) {
    Namespaced(namespace, this).configure()
}

/**
 * Allows defining actions that should run at a specific phase during startup
 *
 * Within its context, invoke a [GearyPhase] to run something during it, ex:
 *
 * ```
 * GearyLoadPhase.ENABLE {
 *     // run code here
 * }
 * ```
 */
fun GearyModule.onPhase(phase: GearyPhase, run: () -> Unit) {
    pipeline.runOnOrAfter(phase, run)
}

/** Runs a block during [GearyPhase.INIT_COMPONENTS] */
fun GearyModule.components(configure: GearyModule.() -> Unit) {
    onPhase(GearyPhase.INIT_COMPONENTS) {
        configure()
    }
}

/** Runs a block during [GearyPhase.INIT_SYSTEMS] */
fun GearyModule.systems(configure: GearyModule.() -> Unit) {
    onPhase(GearyPhase.INIT_SYSTEMS) {
        configure()
    }
}

/** Runs a block during [GearyPhase.INIT_ENTITIES] */
fun GearyModule.entities(configure: GearyModule.() -> Unit) {
    onPhase(GearyPhase.INIT_ENTITIES) {
        configure()
    }
}
