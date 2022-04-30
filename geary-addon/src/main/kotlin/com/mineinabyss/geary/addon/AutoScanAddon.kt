package com.mineinabyss.geary.addon

import com.mineinabyss.geary.addon.GearyLoadPhase.REGISTER_SERIALIZERS
import com.mineinabyss.geary.annotations.AutoScan
import com.mineinabyss.geary.annotations.ExcludeAutoScan
import com.mineinabyss.geary.datatypes.GearyComponent
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.systems.GearySystem
import com.mineinabyss.idofront.autoscan.AutoScanner
import com.mineinabyss.idofront.messaging.logError
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.serializerOrNull
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.logger.Logger
import org.reflections.Reflections
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf

public class AutoScanAddon(
    pkg: String,
    private val serializationAddon: SerializationAddon,
    private val gearyAddon: GearyAddon,
) : KoinComponent {
    private val logger: Logger by inject()

    @PublishedApi
    internal val reflections: Reflections by lazy {
        Reflections(
            ConfigurationBuilder()
                .addClassLoader(gearyAddon.classLoader)
                .addUrls(ClasspathHelper.forPackage(pkg, gearyAddon.classLoader))
        )
    }

    /**
     * Automatically scans for all annotated components
     *
     * @see autoScanComponents
     * @see autoScanSystems
     */
    public fun all() {
        components()
        systems()
    }

    /**
     * Registers serializers for [GearyComponent]s on the classpath of [plugin]'s [ClassLoader].
     *
     * @see AutoScanner
     */
    public fun components(): Unit = with(serializationAddon) {
        reflections.getTypesAnnotatedWith(Serializable::class.java)
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
    public fun systems(): Unit = with(serializationAddon) {
        reflections
            .getTypesAnnotatedWith(AutoScan::class.java)
            ?.asSequence()
            ?.map { it.kotlin }
            ?.filter { it.isSubclassOf(GearySystem::class) }
            ?.mapNotNull { it.objectInstance ?: runCatching { it.createInstance() }.getOrNull() }
            ?.filterIsInstance<GearySystem>()
            ?.toList() // Inline so we can use suspending function
            ?.onEach { gearyAddon.system(it) }
            ?.map { it::class.simpleName }
            ?.joinToString()
            ?.let { logger.info("Autoscan loaded singleton systems: $it") }
    }

    /**
     * Registers serializers for any type [T] on the classpath of [plugin]'s [ClassLoader].
     *
     * @see AutoScanner
     */
    public inline fun <reified T : Any> custom(
        noinline addSubclass: SerializerRegistry<T> = { kClass, serializer ->
            if (serializer != null)
                subclass(kClass, serializer)
            serializer != null
        }
    ) {
        reflections.getSubTypesOf(T::class.java)?.registerSerializers(T::class, addSubclass)
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
    ): Unit = with(serializationAddon) {
        module {
            polymorphic(kClass) {
                asSequence().map { it.kotlin }
                    .filter { !it.hasAnnotation<ExcludeAutoScan>() }
                    .filterIsInstance<KClass<T>>()
                    .filter { kClass ->
                        runCatching {
                            this@polymorphic.addSubclass(kClass, kClass.serializerOrNull())
                        }.onFailure { logError("Failed to load serializer for class ${kClass.simpleName}") }
                            .getOrThrow()
                    }
                    .map { it.simpleName }
                    .joinToString()
                    .also { logger.info("Autoscan loaded serializers for class ${kClass.simpleName}: $it") }
            }
        }
    }
}

public inline fun GearyAddon.autoscan(pkg: String, crossinline init: AutoScanAddon.() -> Unit) {
    startup {
        REGISTER_SERIALIZERS {
            AutoScanAddon(
                pkg = pkg,
                serializationAddon = SerializationAddon(this@autoscan),
                gearyAddon = this@autoscan
            ).init()
        }
    }
}
