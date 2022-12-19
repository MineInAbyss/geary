package com.mineinabyss.geary.addon

import com.mineinabyss.geary.addons.dsl.GearyAddon
import com.mineinabyss.geary.addons.dsl.SerializerRegistry
import com.mineinabyss.geary.addons.dsl.serializers.SerializationAddon
import com.mineinabyss.geary.annotations.AutoScan
import com.mineinabyss.geary.annotations.ExcludeAutoScan
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.datatypes.Component
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.systems.GearySystem
import com.mineinabyss.idofront.autoscan.AutoScanner
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.serializerOrNull
import org.reflections.Reflections
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf

class AutoScanAddon(
    pkg: String,
    private val serializationAddon: SerializationAddon,
    private val gearyAddon: GearyAddon,
) {
    private val logger get() = geary.logger

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
    fun all() {
        components()
        systems()
    }

    /**
     * Registers serializers for [Component]s on the classpath of [plugin]'s [ClassLoader].
     *
     * @see AutoScanner
     */
    fun components(): Unit = with(serializationAddon) {
        reflections.getTypesAnnotatedWith(Serializable::class.java)
            ?.registerSerializers(Component::class) { kClass, serializer ->
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
    fun systems(): Unit = with(serializationAddon) {
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
    inline fun <reified T : Any> custom(
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
    fun <T : Any> Collection<Class<*>>.registerSerializers(
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
                        }.onFailure { logger.severe("Failed to load serializer for class ${kClass.simpleName}") }
                            .getOrThrow()
                    }
                    .map { it.simpleName }
                    .joinToString()
                    .also { logger.info("Autoscan loaded serializers for class ${kClass.simpleName}: $it") }
            }
        }
    }
}
