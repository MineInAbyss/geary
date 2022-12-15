package com.mineinabyss.geary.addon

import com.mineinabyss.geary.addon.modules.addons
import com.mineinabyss.geary.context.geary
import com.mineinabyss.geary.prefabs.PrefabManager
import com.mineinabyss.geary.systems.GearySystem
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlin.reflect.KClass

@DslMarker
@Retention(AnnotationRetention.SOURCE)
annotation class GearyAddonDSL

/**
 * The entry point for other plugins to hook into Geary. Allows registering serializable components, systems, actions,
 * and more.
 */
@GearyAddonDSL
class GearyAddon(
    val namespace: String,
    val classLoader: ClassLoader
) {
    private val addonManager: GearyAddonManager get() = addons.manager
    private val prefabManager: PrefabManager  get() = com.mineinabyss.geary.prefabs.modules.prefabs.manager

    /** Registers a [system]. */
    fun system(system: GearySystem) {
        geary.systems.add(system)
    }

    /** Registers a list of [systems]. */
    fun systems(vararg systems: GearySystem) {
        systems.forEach { system(it) }
    }

    inner class PhaseCreator {
        operator fun GearyLoadPhase.invoke(run: () -> Unit) {
            addonManager.add(this, run)
        }
    }

    /**
     * Allows defining actions that should run at a specific phase during startup
     *
     * Within its context, invoke a [GearyLoadPhase] to run something during it, ex:
     *
     * ```
     * GearyLoadPhase.ENABLE {
     *     // run code here
     * }
     * ```
     */
    inline fun startup(run: PhaseCreator.() -> Unit) {
        PhaseCreator().apply(run)
    }
}

/** The polymorphic builder scope that allows registering subclasses. */
typealias SerializerRegistry<T> = PolymorphicModuleBuilder<T>.(kClass: KClass<T>, serializer: KSerializer<T>?) -> Boolean
