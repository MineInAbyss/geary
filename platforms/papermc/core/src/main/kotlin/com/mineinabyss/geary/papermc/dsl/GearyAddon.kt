package com.mineinabyss.geary.papermc.dsl

import com.mineinabyss.geary.api.addon.AbstractGearyAddon
import com.mineinabyss.geary.api.addon.GearyLoadPhase
import com.mineinabyss.geary.autoscan.AutoScan
import com.mineinabyss.geary.autoscan.ExcludeAutoScan
import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.systems.GearySystem
import com.mineinabyss.geary.ecs.serialization.Formats
import com.mineinabyss.geary.papermc.GearyMCKoinComponent
import com.mineinabyss.geary.papermc.GearyScope
import com.mineinabyss.geary.prefabs.PrefabKey
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.serializerOrNull
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin
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
    public val plugin: Plugin,
) : AbstractGearyAddon(), GearyScope by GearyMCKoinComponent() {
    override val namespace: String = plugin.name.lowercase()

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
}

/** The polymorphic builder scope that allows registering subclasses. */
public typealias SerializerRegistry<T> = PolymorphicModuleBuilder<T>.(kClass: KClass<T>, serializer: KSerializer<T>?) -> Boolean

/** Entry point to register a new [Plugin] with the Geary ECS. */
//TODO support plugins being re-registered after a reload
public inline fun Plugin.gearyAddon(init: GearyAddon.() -> Unit) {
    Formats.clearSerializerModule(name)
    GearyAddon(this).apply(init)
}
