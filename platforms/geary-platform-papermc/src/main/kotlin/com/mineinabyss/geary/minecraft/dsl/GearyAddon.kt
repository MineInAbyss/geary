package com.mineinabyss.geary.minecraft.dsl

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.autoscan.AutoscanComponent
import com.mineinabyss.geary.ecs.api.autoscan.ExcludeAutoscan
import com.mineinabyss.geary.ecs.api.conditions.GearyCondition
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.systems.GearySystem
import com.mineinabyss.geary.ecs.prefab.PrefabManager
import com.mineinabyss.geary.ecs.serialization.Formats
import com.mineinabyss.geary.minecraft.access.BukkitEntityAssociations
import com.mineinabyss.idofront.messaging.logWarn
import com.mineinabyss.idofront.plugin.registerEvents
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.*
import kotlinx.serialization.serializerOrNull
import org.bukkit.entity.Entity
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.FilterBuilder
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
    /**
     * Automatically scans for all annotated components, actions, and conditions.
     *
     * @see autoscanComponents
     * @see autoscanActions
     * @see autoscanConditions
     */
    public fun autoscanAll() {
        autoscanComponents()
        autoscanActions()
        autoscanConditions()
//        autoscanSingletonSystems()
    }

    /**
     * Registers serializers for [GearyComponent]s on the classpath of [plugin]'s [ClassLoader].
     *
     * @see AutoScanner
     */
    public fun autoscanComponents(init: AutoScanner.() -> Unit = {}) {
        autoscan<GearyComponent>({
            getBy = {
                getTypesAnnotatedWith(AutoscanComponent::class.java)
            }
            filterBy = {
                filter { it.hasAnnotation<Serializable>() }
            }

            init()
        }) { kClass, serializer ->
            component(kClass, serializer)
        }
    }

    /**
     * Registers serializers for [GearyAction]s on the classpath of [plugin]'s [ClassLoader].
     *
     * @see AutoScanner
     */
    public fun autoscanActions(init: AutoScanner.() -> Unit = {}) {
        autoscan<GearyAction>(init)
    }

    /**
     * Registers serializers for [GearyCondition]s on the classpath of [plugin]'s [ClassLoader].
     *
     * @see AutoScanner
     */
    public fun autoscanConditions(init: AutoScanner.() -> Unit = {}) {
        autoscan<GearyCondition>(init)
    }

//    public fun autoscanSingletonSystems(init: AutoScanner.() -> Unit = {}) {
//        autoscan<GearySystem>({
//            filterBy = { this }
//            init()
//        }) { kClass, _ ->
//            kClass.objectInstance?.let { system(it) }
//        }
//    }

    /**
     * Registers serializers for any type [T] on the classpath of [plugin]'s [ClassLoader].
     *
     * @see AutoScanner
     */
    public inline fun <reified T : Any> autoscan(
        crossinline init: AutoScanner.() -> Unit = {},
        runNow: Boolean = false,
        noinline addSubclass: SerializerRegistry<T> = { kClass, serializer ->
            if (serializer != null)
                subclass(kClass, serializer)
        }
    ) {
        if (runNow) AutoScanner().apply(init).registerSerializers(T::class, addSubclass)
        else startup {
            GearyLoadPhase.REGISTER_SERIALIZERS {
                AutoScanner().apply(init).registerSerializers(T::class, addSubclass)
            }
        }
    }

    private companion object {
        private data class CacheKey(val classLoader: ClassLoader, val path: String?, val excluded: Collection<String>)

        private val reflectionsCache = mutableMapOf<CacheKey, Reflections>()
    }

    /**
     * DSL for configuring automatic scanning of classes to be registered into Geary's [SerializersModule].
     *
     * A [path] to limit search to may be specified. Specific packages can also be excluded with [excludePath].
     * Annotate a class with [ExcludeAutoscan] to exclude it from automatically being registered.
     *
     * _Note that if the plugin is loaded using a custom classloading solution, autoscan may not work._
     *
     * @property path Optional path to restrict what packages are scanned.
     * @property excluded Excluded paths under [path].
     */
    @GearyAddonDSL
    public inner class AutoScanner {
        public var path: String? = null
        internal var getBy: Reflections.(kClass: KClass<*>) -> Set<Class<*>> = { kClass ->
            getSubTypesOf(kClass.java)
        }
        internal var filterBy: List<KClass<*>>.() -> List<KClass<*>> = {
            filter { it.hasAnnotation<Serializable>() }
        }
        private val excluded = mutableListOf<String>()

        /** Add a path to be excluded from the scanner. */
        public fun excludePath(path: String) {
            excluded += path
        }

        /** Gets a reflections object under [path] */
        private fun getReflections(): Reflections? {
            val classLoader = this@GearyAddon.plugin::class.java.classLoader
            // cache the object we get because it takes considerable amount of time to get
            val cacheKey = CacheKey(classLoader, path, excluded)
            reflectionsCache[cacheKey]?.let { return it }

            val reflections = Reflections(
                ConfigurationBuilder()
                    .addClassLoader(classLoader)
                    .addUrls(ClasspathHelper.forClassLoader(classLoader))
                    .addScanners(SubTypesScanner())
                    .filterInputsBy(FilterBuilder().apply {
                        if (path != null) includePackage(path)
                        excluded.forEach { excludePackage(it) }
                    })
            )

            reflectionsCache[cacheKey] = reflections

            // Check if the store is empty. Since we only use a single SubTypesScanner, if this is empty
            // then the path passed in returned 0 matches.
            if (reflections.store.keySet().isEmpty()) {
                logWarn("Autoscanner failed to find classes for ${this@GearyAddon.plugin.name}${if (path == null) "" else " in package ${path}}"}.")
                return null
            }
            return reflections
        }


        /** Helper function to register serializers via scanning for geary classes. */
        @OptIn(InternalSerializationApi::class)
        public fun <T : Any> registerSerializers(
            kClass: KClass<T>,
            addSubclass: SerializerRegistry<T> = { kClass, serializer ->
                if (serializer != null)
                    subclass(kClass, serializer)
            },
        ) {
            val reflections = getReflections() ?: return
            this@GearyAddon.serializers {
                polymorphic(kClass) {
                    reflections.getBy(kClass)
                        .map { it.kotlin }
                        .apply { filterBy() }
                        .filter { !it.hasAnnotation<ExcludeAutoscan>() }
                        .filterIsInstance<KClass<T>>()
                        .map {
                            this@polymorphic.addSubclass(it, it.serializerOrNull())
                            it.simpleName
                        }
                        .joinToString()
                        .also { this@GearyAddon.plugin.logger.info("Autoscan loaded serializers for class ${kClass.simpleName}: $it") }
                }
            }
        }
    }


    /** Adds a [SerializersModule] for polymorphic serialization of [GearyComponent]s within the ECS. */
    public inline fun components(crossinline init: PolymorphicModuleBuilder<GearyComponent>.() -> Unit) {
        serializers { polymorphic(GearyComponent::class) { init() } }
    }

    /** Adds a [SerializersModule] for polymorphic serialization of [GearyAction]s within the ECS. */
    public inline fun actions(crossinline init: PolymorphicModuleBuilder<GearyAction>.() -> Unit) {
        serializers { polymorphic(GearyAction::class) { init() } }
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

    /** Adds a serializable action. */
    public inline fun <reified T : GearyAction> PolymorphicModuleBuilder<T>.action(serializer: KSerializer<T>) {
        subclass(serializer)
    }

    /** Adds a serializable action. */
    public fun <T : GearyAction> PolymorphicModuleBuilder<T>.action(kClass: KClass<T>, serializer: KSerializer<T>) {
        subclass(kClass, serializer)
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
    ) {
        Engine.registerComponentIdForClass(kClass)

        val serialName = serializer?.descriptor?.serialName ?: return
        if (!Formats.isRegistered(serialName)) {
            Formats.registerSerialName(serialName, kClass)
            subclass(kClass, serializer)
        }
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
public typealias SerializerRegistry<T> = PolymorphicModuleBuilder<T>.(kClass: KClass<T>, serializer: KSerializer<T>?) -> Unit

/** Entry point to register a new [Plugin] with the Geary ECS. */
//TODO support plugins being re-registered after a reload
public inline fun Plugin.gearyAddon(init: GearyAddon.() -> Unit) {
    Formats.clearSerializerModule(name)
    GearyAddon(this).apply(init)
}
