package com.mineinabyss.geary.minecraft.dsl

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.autoscan.AutoScan
import com.mineinabyss.geary.ecs.api.autoscan.ExcludeAutoScan
import com.mineinabyss.geary.ecs.api.engine.globalEngine
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.systems.GearySystem
import com.mineinabyss.geary.ecs.serialization.Formats
import com.mineinabyss.geary.minecraft.access.BukkitEntityAssociations
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.PrefabManager
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.serializerOrNull
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf

@DslMarker
internal annotation class GearyAddonDSL

//TODO make a reusable solution for addons within idofront
/**
 * The entry point for other plugins to hook into Geary. Allows registering serializable components, systems, actions,
 * and more.
 */
@GearyAddonDSL
public class GearyAddon(
    public val plugin: Plugin
) {
    public val classLoader: ClassLoader = plugin::class.java.classLoader

    /**
     * Automatically scans for all annotated components
     *
     * @see autoScanComponents
     * @see autoScanSystems
     */
    public fun autoScanAll() {
        startup {
            GearyLoadPhase.REGISTER_SERIALIZERS {
                autoScanComponents()
                autoScanSystems()
            }
        }
    }

    /**
     * Registers serializers for [GearyComponent]s on the classpath of [plugin]'s [ClassLoader].
     *
     * @see AutoScanner
     */
    public fun autoScanComponents() {
        AutoScanner(classLoader).getReflections()?.getTypesAnnotatedWith(Serializable::class.java)
            ?.registerSerializers(GearyComponent::class) { kClass, serializer ->
                val serialName = serializer?.descriptor?.serialName ?: return@registerSerializers false
                PrefabKey.ofOrNull(serialName) ?: return@registerSerializers false
                component(kClass, serializer)
            }
    }

    /**
     * Registers any systems (including event listeners) that are annotated with [AutoScan].
     *
     * Supports singletons or classes with no constructor parameters.
     *
     * @see AutoScanner
     */
    public fun autoScanSystems() {
        AutoScanner(classLoader).getReflections()
            ?.getTypesAnnotatedWith(AutoScan::class.java)
            ?.asSequence()
            ?.map { it.kotlin }
            ?.filter { it.isSubclassOf(GearySystem::class) }
            ?.mapNotNull { it.objectInstance ?: runCatching { it.createInstance() }.getOrNull() }
            ?.filterIsInstance<GearySystem>()
            ?.onEach { system(it) }
            ?.map { it::class.simpleName }
            ?.joinToString()
            ?.let { this@GearyAddon.plugin.logger.info("Autoscan loaded singleton systems: $it") }
    }

    /**
     * Registers serializers for any type [T] on the classpath of [plugin]'s [ClassLoader].
     *
     * @see AutoScanner
     */
    public inline fun <reified T : Any> autoScan(
        crossinline init: AutoScanner.() -> Unit = {},
        noinline addSubclass: SerializerRegistry<T> = { kClass, serializer ->
            if (serializer != null)
                subclass(kClass, serializer)
            serializer != null
        }
    ) {
        mutableListOf("") += listOf("")
        AutoScanner(classLoader).apply(init).getReflections()
            ?.getSubTypesOf(T::class.java)?.registerSerializers(T::class, addSubclass)
    }

    /** Helper function to register serializers via scanning for geary classes. */
    @OptIn(InternalSerializationApi::class)
    public fun <T : Any> Collection<Class<*>>.registerSerializers(
        kClass: KClass<T>,
        addSubclass: SerializerRegistry<T> = { subClass, serializer ->
            if (serializer != null)
                subclass(subClass, serializer)
            serializer != null
        },
    ) {
        this@GearyAddon.serializers {
            polymorphic(kClass) {
                asSequence().map { it.kotlin }
                    .filter { !it.hasAnnotation<ExcludeAutoScan>() }
                    .filterIsInstance<KClass<T>>()
                    .filter { this@polymorphic.addSubclass(it, it.serializerOrNull()) }
                    .map { it.simpleName }
                    .joinToString()
                    .also { this@GearyAddon.plugin.logger.info("Autoscan loaded serializers for class ${kClass.simpleName}: $it") }
            }
        }
    }

    /** Adds a [SerializersModule] for polymorphic serialization of [GearyComponent]s within the ECS. */
    public inline fun components(crossinline init: PolymorphicModuleBuilder<GearyComponent>.() -> Unit) {
        serializers { polymorphic(GearyComponent::class) { init() } }
    }

    /** Registers a [system]. */
    public fun system(system: GearySystem) {
        globalEngine.addSystem(system)
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
        Formats.addSerializerModule(plugin.name, SerializersModule { init() })
    }

    /** Entry point for extending behaviour regarding how bukkit entities are linked to the ECS. */
    public inline fun bukkitEntityAssociations(init: BukkitEntityAssociationsAddon.() -> Unit) {
        BukkitEntityAssociationsAddon().apply(init)
    }

    /** Entry point for extending behaviour regarding how bukkit entities are linked to the ECS. */
    public class BukkitEntityAssociationsAddon {
        /** Additional things to do or components to be added to an [Entity] of type [T] is registered with the ECS. */
        public inline fun <reified T : Entity> onEntityRegister(crossinline run: GearyEntity.(T) -> Unit) {
            BukkitEntityAssociations.onBukkitEntityRegister { entity ->
                if (entity is T) run(entity)
            }
        }

        /** Additional things to do before an [Entity] of type [T] is removed from the ECS (or Minecraft World). */
        public inline fun <reified T : Entity> onEntityUnregister(crossinline run: GearyEntity.(T) -> Unit) {
            BukkitEntityAssociations.onBukkitEntityUnregister { entity ->
                if (entity is T) run(entity)
            }
        }
    }

    /** Loads prefab entities from all files inside a [directory][from], into a given [namespace] */
    public fun loadPrefabs(
        from: File,
        namespace: String = plugin.name.lowercase()
    ) {
        startup {
            GearyLoadPhase.LOAD_PREFABS {
                // Start with the innermost directories
                val dirs = from.walkBottomUp().filter { it.isDirectory }
                val files = dirs.flatMap { dir -> dir.walk().maxDepth(1).filter { it.isFile } }
                files.forEach { file ->
                    val entity = PrefabManager.loadFromFile(namespace, file) ?: return@forEach
                    GearyLoadManager.loadingPrefabs += entity
                }
            }
        }
    }

    public class PhaseCreator {
        public operator fun GearyLoadPhase.invoke(run: () -> Unit) {
            GearyLoadManager.add(this, run)
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

/** Entry point to register a new [Plugin] with the Geary ECS. */
//TODO support plugins being re-registered after a reload
public inline fun Plugin.gearyAddon(init: GearyAddon.() -> Unit) {
    Formats.clearSerializerModule(name)
    GearyAddon(this).apply(init)
}
