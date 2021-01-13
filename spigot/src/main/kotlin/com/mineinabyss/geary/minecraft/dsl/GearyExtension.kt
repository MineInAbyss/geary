package com.mineinabyss.geary.minecraft.dsl

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.actions.GearyAction
import com.mineinabyss.geary.ecs.autoscan.ExcludeAutoscan
import com.mineinabyss.geary.ecs.conditions.GearyCondition
import com.mineinabyss.geary.ecs.engine.Engine
import com.mineinabyss.geary.ecs.serialization.Formats
import com.mineinabyss.geary.ecs.systems.TickingSystem
import com.mineinabyss.geary.ecs.types.EntityTypeManager
import com.mineinabyss.geary.ecs.types.GearyEntityType
import com.mineinabyss.geary.ecs.types.GearyEntityTypes
import com.mineinabyss.geary.minecraft.store.BukkitEntityAccess
import com.mineinabyss.idofront.messaging.logVal
import com.mineinabyss.idofront.messaging.logWarn
import kotlinx.serialization.*
import kotlinx.serialization.modules.*
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.FilterBuilder
import kotlin.reflect.KClass
import kotlin.reflect.full.hasAnnotation

@DslMarker
internal annotation class GearyExtensionDSL

//TODO make a reusable solution for extensions within idofront
/**
 * The entry point for other plugins to hook into Geary. Allows registering serializable components, systems, actions,
 * and more.
 */
@GearyExtensionDSL
public class GearyExtension(
    public val plugin: Plugin,
    types: GearyEntityTypes<out GearyEntityType>?,
) {
    init {
        if (types != null)
            EntityTypeManager.add(plugin.name, types)
    }

    /**
     * Registers serializers for [GearyComponent]s on the classpath of [plugin]'s [ClassLoader].
     *
     * @see AutoScanner
     */
    @InternalSerializationApi
    public fun autoscanComponents(init: AutoScanner.() -> Unit = {}) {
        autoscan<GearyComponent>(init) { kClass, serializer ->
            component(kClass, serializer)
        }
    }

    /**
     * Registers serializers for [GearyAction]s on the classpath of [plugin]'s [ClassLoader].
     *
     * @see AutoScanner
     */
    @InternalSerializationApi
    public fun autoscanActions(init: AutoScanner.() -> Unit = {}) {
        autoscan<GearyAction>(init) { kClass, serializer ->
            action(kClass, serializer)
        }
    }

    /**
     * Registers serializers for [GearyCondition]s on the classpath of [plugin]'s [ClassLoader].
     *
     * @see AutoScanner
     */
    @InternalSerializationApi
    public fun autoscanConditions(init: AutoScanner.() -> Unit = {}) {
        autoscan<GearyCondition>(init)
    }

    //TODO basically use the same system except get different annotations and not kSerializer
    /*public fun autoscanSingletonSystems(init: AutoScanner.() -> Unit = {}) {
        val scanner = AutoScanner().apply(init)
        scanner.registerSerializers<TickingSystem, GearySystem> { kClass, _ ->
            kClass.objectInstance?.let { system(it) }
        }
    }*/

    /**
     * Registers serializers for any type [T] on the classpath of [plugin]'s [ClassLoader].
     *
     * @see AutoScanner
     */
    @InternalSerializationApi
    public inline fun <reified T : Any> autoscan(
        init: AutoScanner.() -> Unit = {},
        noinline addSubclass: SerializerRegistry<T> = { kClass, serializer -> subclass(kClass, serializer) }
    ) {
        AutoScanner().apply(init).registerSerializers(T::class, addSubclass)
    }

    private companion object {
        private data class CacheKey(val plugin: Plugin, val path: String?, val excluded: Collection<String>)

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
    @GearyExtensionDSL
    public inner class AutoScanner {
        public var path: String? = null
        private val excluded = mutableListOf<String>()

        /** Add a path to be excluded from the scanner. */
        public fun excludePath(path: String) {
            excluded += path
        }

        /** Gets a reflections object under [path] */
        private fun getReflections(): Reflections? {
            // cache the object we get because it takes considerable amount of time to get
            val cacheKey = CacheKey(this@GearyExtension.plugin, path, excluded)
            reflectionsCache[cacheKey]?.let { return it }
            val classLoader = this@GearyExtension.plugin::class.java.classLoader
            val reflections = Reflections(
                ConfigurationBuilder()
                    .addClassLoaders(classLoader)
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
                logWarn("Autoscanner failed to find classes for ${this@GearyExtension.plugin.name}${if (path == null) "" else " in package ${path}}"}.")
                return null
            }
            return reflections
        }


        /** Helper function to register serializers via scanning for geary classes. */
        @InternalSerializationApi
        public fun <T : Any> registerSerializers(
            kClass: KClass<T>,
            addSubclass: SerializerRegistry<T> = { kClass, serializer -> subclass(kClass, serializer) },
        ) {
            val reflections = getReflections() ?: return
            this@GearyExtension.serializers {
                polymorphic(kClass) {
                    reflections.getSubTypesOf(kClass.java).toSet()
                        .map { it.kotlin }
                        .filter { it.hasAnnotation<Serializable>() }
                        .filter { !it.hasAnnotation<ExcludeAutoscan>() }
                        .filterIsInstance<KClass<T>>()
                        .map {
                            this@polymorphic.addSubclass(it, it.serializer())
                            it.simpleName
                        }
                        .joinToString()
                        .logVal("Autoscan loaded serializers: ")
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

    /** Registers a list of [systems]. */
    public fun systems(vararg systems: TickingSystem) {
        Engine.addSystems(*systems)
    }

    /** Registers a [system]. */
    public fun system(system: TickingSystem) {
        Engine.addSystems(system)
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
        serializer: KSerializer<T>
    ) {
        val name = serializer.descriptor.serialName
        if (name !in Formats.componentSerialNames) {
            Formats.addSerialName(name, kClass)
            subclass(kClass, serializer)
        }
    }

    /** Adds a [SerializersModule] to be used for polymorphic serialization within the ECS. */
    public fun serializers(init: SerializersModuleBuilder.() -> Unit) {
        Formats.addSerializerModule(SerializersModule { init() })
    }

    /** Entry point for extending behaviour regarding how bukkit entities are linked to the ECS. */
    public fun bukkitEntityAccess(init: BukkitEntityAccessExtension.() -> Unit) {
        BukkitEntityAccessExtension().apply(init)
    }

    /** Entry point for extending behaviour regarding how bukkit entities are linked to the ECS. */
    public class BukkitEntityAccessExtension {
        /** Additional things to do or components to be added to an [Entity] of type [T] is registered with the ECS. */
        public inline fun <reified T : Entity> onEntityRegister(crossinline list: MutableList<GearyComponent>.(T) -> Unit) {
            BukkitEntityAccess.onBukkitEntityRegister { entity ->
                if (entity is T) list(entity)
            }
        }

        /** Additional things to do before an [Entity] of type [T] is removed from the ECS (or Minecraft World). */
        public inline fun <reified T : Entity> onEntityUnregister(crossinline list: (GearyEntity, T) -> Unit) {
            BukkitEntityAccess.onBukkitEntityUnregister { gearyEntity, entity ->
                if (entity is T) list(gearyEntity, entity)
            }
        }

        /**
         * Additional ways of getting a [GearyEntity] given a spigot [Entity]. Will try one by one until a conversion
         * is not null. There is currently no priority system.
         */
        //TODO priority system
        public fun entityConversion(getter: Entity.() -> GearyEntity?) {
            BukkitEntityAccess.bukkitEntityAccessExtensions += getter
        }
    }
}

public typealias SerializerRegistry<T> = PolymorphicModuleBuilder<T>.(kClass: KClass<T>, serializer: KSerializer<T>) -> Unit

/**
 * Entry point to register a new [Plugin] with the Geary ECS.
 *
 * @param types The subclass of [GearyEntityTypes] associated with this plugin.
 */
public inline fun <reified T : GearyEntityType> Plugin.attachToGeary(
    types: GearyEntityTypes<T>? = null,
    init: GearyExtension.() -> Unit
) {
    //TODO support plugins being re-registered after a reload
    GearyExtension(this, types).apply {
        components {
            // Whenever we're using this serial module to deserialize our components we want to access them by
            // reference through geary, not by using the actual EntityType's serializer like we would when
            // reading config files.
            component(GearyEntityType.ByReferenceSerializer(T::class))
        }
    }.apply(init)
}
