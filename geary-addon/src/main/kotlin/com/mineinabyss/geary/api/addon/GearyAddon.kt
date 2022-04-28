package com.mineinabyss.geary.api.addon

import com.mineinabyss.geary.context.GearyContext
import com.mineinabyss.geary.context.GearyContextKoin
import com.mineinabyss.geary.prefabs.PrefabManager
import com.mineinabyss.geary.prefabs.PrefabManagerContext
import com.mineinabyss.geary.systems.GearySystem
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import org.koin.core.component.inject
import kotlin.reflect.KClass

@DslMarker
@Retention(AnnotationRetention.SOURCE)
public annotation class GearyAddonDSL

/**
 * The entry point for other plugins to hook into Geary. Allows registering serializable components, systems, actions,
 * and more.
 */
@GearyAddonDSL
public class GearyAddon(
    public val namespace: String
) : GearyContext by GearyContextKoin(), AbstractAddonManagerContext, PrefabManagerContext {
    override val addonManager: AbstractAddonManager by inject()
    override val prefabManager: PrefabManager by inject()

    /** Registers a [system]. */
    public fun system(system: GearySystem) {
        engine.addSystem(system)
    }

    /** Registers a list of [systems]. */
    public fun systems(vararg systems: GearySystem) {
        systems.forEach { system(it) }
    }

    public inner class PhaseCreator {
        public operator fun GearyLoadPhase.invoke(run: suspend () -> Unit) {
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
    public inline fun startup(run: PhaseCreator.() -> Unit) {
        PhaseCreator().apply(run)
    }
}

/** The polymorphic builder scope that allows registering subclasses. */
public typealias SerializerRegistry<T> = PolymorphicModuleBuilder<T>.(kClass: KClass<T>, serializer: KSerializer<T>?) -> Boolean
