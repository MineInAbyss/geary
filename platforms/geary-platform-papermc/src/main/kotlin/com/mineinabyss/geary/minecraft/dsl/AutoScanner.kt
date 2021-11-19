package com.mineinabyss.geary.minecraft.dsl

import com.mineinabyss.geary.ecs.api.autoscan.ExcludeAutoscan
import com.mineinabyss.idofront.messaging.logWarn
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.serializerOrNull
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.FilterBuilder
import kotlin.reflect.KClass
import kotlin.reflect.full.hasAnnotation

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
public class AutoScanner(private val addon: GearyAddon) {
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
        val classLoader = addon.plugin::class.java.classLoader
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
            logWarn("Autoscanner failed to find classes for ${addon.plugin.name}${if (path == null) "" else " in package ${path}}"}.")
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
        addon.serializers {
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
                    .also { addon.plugin.logger.info("Autoscan loaded serializers for class ${kClass.simpleName}: $it") }
            }
        }
    }

    private companion object {
        private data class CacheKey(val classLoader: ClassLoader, val path: String?, val excluded: Collection<String>)

        private val reflectionsCache = mutableMapOf<CacheKey, Reflections>()
    }
}