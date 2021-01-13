package com.mineinabyss.geary.minecraft.dsl

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.actions.GearyAction
import com.mineinabyss.geary.ecs.autoscan.ExcludeAutoscan
import com.mineinabyss.geary.ecs.engine.Engine
import com.mineinabyss.geary.ecs.serialization.Formats
import com.mineinabyss.geary.ecs.systems.TickingSystem
import com.mineinabyss.geary.ecs.types.EntityTypeManager
import com.mineinabyss.geary.ecs.types.GearyEntityType
import com.mineinabyss.geary.ecs.types.GearyEntityTypes
import com.mineinabyss.geary.minecraft.store.BukkitEntityAccess
import com.mineinabyss.idofront.messaging.logInfo
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

public annotation class GearySystem


//TODO make a reusable solution for extensions within idofront
/**
 * The entry point for other plugins to hook into Geary. Allows registering serializable components, systems, actions,
 * and more.
 */
@GearyExtensionDSL
public class GearyExtension(
    private val plugin: Plugin,
    types: GearyEntityTypes<out GearyEntityType>?,
) {
    init {
        if (types != null)
            EntityTypeManager.add(plugin.name, types)
    }

    /**
     * Scans for [GearyComponent]s in the classpath of the provided [plugin]s [ClassLoader]. Optional
     * [componentPath] can be used to restrict what packages are scanned.
     *
     * _Note that if the plugin is loaded using a custom classloading solution, autoscan may not work._
     */
    @InternalSerializationApi
    public fun autoscanComponents(init: AutoScanner.() -> Unit = {}) {
        AutoScanner().apply(init).registerSerializers<GearyComponent> { kClass, serializer ->
            component(kClass, serializer)
        }
    }

    /**
     * Scans for [GearyAction]s in the classpath of the provided [plugin]s [ClassLoader]. Optional
     * [actionPath] can be used to restrict what packages are scanned.
     *
     * _Note that if the plugin is loaded using a custom classloading solution, autoscan may not work._
     */
    @InternalSerializationApi
    public fun autoscanActions(init: AutoScanner.() -> Unit = {}) {
        AutoScanner().apply(init).registerSerializers<GearyAction> { kClass, serializer ->
            action(kClass, serializer)
        }
    }

    /*public fun autoscanSingletonSystems(init: AutoScanner.() -> Unit = {}) {
        val scanner = AutoScanner().apply(init)
        scanner.registerSerializers<TickingSystem, GearySystem> { kClass, _ ->
            kClass.objectInstance?.let { system(it) }
        }
    }*/

    public fun getReflections(path: String?, excluded: MutableList<String>): Reflections? {
        val reflections = Reflections(
            ConfigurationBuilder()
                .addClassLoaders(plugin::class.java.classLoader)
                .addUrls(ClasspathHelper.forClassLoader(plugin::class.java.classLoader))
                .addScanners(SubTypesScanner())
                .apply {
                    path?.let {
                        filterInputsBy(FilterBuilder().includePackage(path)
                            .apply {
                                excluded.forEach { excludePackage(it) }
                            })
                    }
                }
        )

        // Check if the store is empty. Since we only use a single SubTypesScanner, if this is empty
        // then the path passed in returned 0 matches.
        if (reflections.store.keySet().isEmpty()) return null
        return reflections
    }

    @GearyExtensionDSL
    public inner class AutoScanner {
        public var path: String? = null
        private val excluded = mutableListOf<String>()

        public fun excludePath(path: String) {
            excluded += path
        }

        /**
         * Helper function to register serializers via scanning for geary classes.
         */
        @InternalSerializationApi
        internal inline fun <reified T : Any> registerSerializers(
            crossinline addSubclass: PolymorphicModuleBuilder<T>.(kClass: KClass<T>, serializer: KSerializer<T>) -> Unit,
        ) {
            val reflections = this@GearyExtension.getReflections(path, excluded)

            if (reflections == null) {
                logWarn("No ${T::class.simpleName}s found for ${this@GearyExtension.plugin.name}${if (path == null) "" else " in package ${path}}"}.")
                return
            }

            this@GearyExtension.serializers {
                polymorphic(T::class) {
                    reflections.getSubTypesOf(T::class.java).toSet()
                        .map { it.kotlin }
                        .filter { it.hasAnnotation<Serializable>() }
                        .filter { !it.hasAnnotation<ExcludeAutoscan>() }
                        .filterIsInstance<KClass<T>>()
                        .forEach {
                            this@polymorphic.addSubclass(it, it.serializer())
                            logInfo("Autoscan loaded serializer for ${it.qualifiedName}.")
                        }
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
