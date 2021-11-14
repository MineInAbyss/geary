package com.mineinabyss.geary.minecraft.dsl

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.autoscan.AutoscanComponent
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.systems.GearySystem
import com.mineinabyss.geary.ecs.prefab.PrefabManager
import com.mineinabyss.geary.ecs.serialization.Formats
import com.mineinabyss.geary.minecraft.access.BukkitEntityAssociations
import com.mineinabyss.idofront.plugin.registerEvents
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
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
    /**
     * Automatically scans for all annotated components, actions, and conditions.
     *
     * @see autoscanComponents
     * @see autoscanActions
     * @see autoscanConditions
     */
    public fun autoscanAll() {
        autoscanComponents()
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
        if (runNow) AutoScanner(this).apply(init).registerSerializers(T::class, addSubclass)
        else startup {
            GearyLoadPhase.REGISTER_SERIALIZERS {
                AutoScanner(this@GearyAddon).apply(init).registerSerializers(T::class, addSubclass)
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
