package com.mineinabyss.geary.minecraft.dsl

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.autoscan.ExcludeAutoscan
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.systems.GearySystem
import com.mineinabyss.geary.ecs.prefab.PrefabKey
import com.mineinabyss.geary.ecs.prefab.PrefabManager
import com.mineinabyss.geary.ecs.serialization.Formats
import com.mineinabyss.geary.minecraft.access.BukkitEntityAssociations
import com.mineinabyss.idofront.plugin.registerEvents
import kotlinx.serialization.*
import kotlinx.serialization.modules.*
import org.bukkit.entity.Entity
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.full.hasAnnotation

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
     * @see autoscanComponents
     * @see autoscanActions
     */
    public fun autoscanAll() {
        startup {
            GearyLoadPhase.REGISTER_SERIALIZERS {
                autoscanComponents()
                autoscanSingletonSystems()
            }
        }
    }

    /**
     * Registers serializers for [GearyComponent]s on the classpath of [plugin]'s [ClassLoader].
     *
     * @see AutoScanner
     */
    public fun autoscanComponents() {
        AutoScanner(classLoader).getReflections()?.getTypesAnnotatedWith(Serializable::class.java)
            ?.registerSerializers(GearyComponent::class) { kClass, serializer ->
                val serialName = serializer?.descriptor?.serialName ?: return@registerSerializers false
                PrefabKey.ofOrNull(serialName) ?: return@registerSerializers false
                component(kClass, serializer)
            }
    }

    public fun autoscanSingletonSystems() {
        AutoScanner(classLoader).getReflections()?.getSubTypesOf(GearySystem::class.java)
            ?.mapNotNull { it.kotlin.objectInstance as? GearySystem }
            ?.onEach { system(it) }
            ?.map { it::class.simpleName }
            ?.joinToString()
            ?.also { this@GearyAddon.plugin.logger.info("Autoscan loaded singleton systems: $it") }
    }

    /**
     * Registers serializers for any type [T] on the classpath of [plugin]'s [ClassLoader].
     *
     * @see AutoScanner
     */
    public inline fun <reified T : Any> autoscan(
        crossinline init: AutoScanner.() -> Unit = {},
        noinline addSubclass: SerializerRegistry<T> = { kClass, serializer ->
            if (serializer != null)
                subclass(kClass, serializer)
            serializer != null
        }
    ) {
        AutoScanner(classLoader).apply(init).getReflections()
            ?.getSubTypesOf(T::class.java)?.registerSerializers(T::class, addSubclass)
    }

    /** Helper function to register serializers via scanning for geary classes. */
    @OptIn(InternalSerializationApi::class)
    public fun <T : Any> Collection<Class<*>>.registerSerializers(
        kClass: KClass<T>,
        addSubclass: SerializerRegistry<T> = { kClass, serializer ->
            if (serializer != null)
                subclass(kClass, serializer)
            serializer != null
        },
    ) {
        this@GearyAddon.serializers {
            polymorphic(kClass) {
                map { it.kotlin }
                    .filter { !it.hasAnnotation<ExcludeAutoscan>() }
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
        Engine.addSystem(system)
    }

    /** Registers a list of [systems]. */
    public fun systems(vararg systems: GearySystem) {
        plugin.registerEvents(*systems.filterIsInstance<Listener>().toTypedArray())
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
                val files = dirs.flatMap { it.walk().maxDepth(1).filter { it.isFile } }
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
