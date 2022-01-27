package com.mineinabyss.geary.api.addon

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.engine.EngineScope
import com.mineinabyss.geary.ecs.api.systems.GearySystem
import com.mineinabyss.geary.ecs.serialization.Formats
import com.mineinabyss.geary.prefabs.PrefabManagerScope
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.modules.polymorphic
import java.io.File
import kotlin.reflect.KClass

@DslMarker
@Retention(AnnotationRetention.SOURCE)
public annotation class GearyAddonDSL

/**
 * The entry point for other plugins to hook into Geary. Allows registering serializable components, systems, actions,
 * and more.
 */
@GearyAddonDSL
public abstract class AbstractGearyAddon : EngineScope, AbstractAddonManagerScope, PrefabManagerScope {
    public abstract val namespace: String

    /** Adds a [SerializersModule] for polymorphic serialization of [GearyComponent]s within the ECS. */
    public inline fun components(crossinline init: PolymorphicModuleBuilder<GearyComponent>.() -> Unit) {
        serializers { polymorphic(GearyComponent::class) { init() } }
    }

    /** Registers a [system]. */
    public fun system(system: GearySystem) {
        engine.addSystem(system)
    }

    /** Registers a list of [systems]. */
    public fun systems(vararg systems: GearySystem) {
        systems.forEach { system(it) }
    }

    /**
     * Adds a serializable component and registers it with Geary to allow finding the appropriate class via
     * component serial name.
     */
    public inline fun <reified T : GearyComponent> PolymorphicModuleBuilder<T>.component(serializer: KSerializer<T>) {
        component(T::class, serializer)
    }

    /**
     * Adds a serializable component and registers it with Geary to allow finding the appropriate class via
     * component serial name.
     */
    public fun <T : GearyComponent> PolymorphicModuleBuilder<T>.component(
        kClass: KClass<T>,
        serializer: KSerializer<T>?
    ): Boolean {
        val serialName = serializer?.descriptor?.serialName ?: return false
        if (!Formats.isRegistered(serialName)) {
            Formats.registerSerialName(serialName, kClass)
            subclass(kClass, serializer)
            return true
        }
        return false
    }

    /** Adds a [SerializersModule] to be used for polymorphic serialization within the ECS. */
    public inline fun serializers(init: SerializersModuleBuilder.() -> Unit) {
        Formats.addSerializerModule(namespace, SerializersModule { init() })
    }

    /** Loads prefab entities from all files inside a [directory][from], into a given [namespace] */
    public fun loadPrefabs(
        from: File,
        namespace: String = this.namespace
    ) {
        startup {
            GearyLoadPhase.LOAD_PREFABS {
                // Start with the innermost directories
                val dirs = from.walkBottomUp().filter { it.isDirectory }
                val files = dirs.flatMap { dir -> dir.walk().maxDepth(1).filter { it.isFile } }
                files.forEach { file ->
                    val entity = prefabManager.loadFromFile(namespace, file) ?: return@forEach
                    addonManager.loadingPrefabs += entity
                }
            }
        }
    }

    public inner class PhaseCreator {
        public operator fun GearyLoadPhase.invoke(run: () -> Unit) {
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
