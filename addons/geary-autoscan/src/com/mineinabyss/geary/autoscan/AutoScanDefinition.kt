package com.mineinabyss.geary.autoscan

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.mineinabyss.geary.serialization.SerializableComponentsModule
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.serializer
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.FilterBuilder
import kotlin.reflect.KClass
import kotlin.reflect.full.hasAnnotation

class AutoScanDefinition(
    val logger: Logger,
    val classLoader: ClassLoader,
    val limitToPackages: List<String>,
    val serializableComponents: SerializableComponentsModule,
) {
    private val reflections: Reflections by lazy {
        Reflections(
            ConfigurationBuilder()
                .apply { limitToPackages.forEach { forPackage(it, classLoader) } }
                .filterInputsBy(FilterBuilder().apply { limitToPackages.forEach { includePackage(it) } })
                .setScanners(Scanners.SubTypes, Scanners.TypesAnnotated, Scanners.MethodsAnnotated)
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
//        systems()
    }

    /**
     * Registers serializers for [com.mineinabyss.geary.datatypes.Component]s.
     *
     * @see AutoScanner
     */
    @OptIn(InternalSerializationApi::class)
    fun components() {
        val scanned = reflections
            .get(Scanners.TypesAnnotated.with(Serializable::class.java).asClass<Class<*>>(classLoader))
            .asSequence()
            .map { it.kotlin }
            .filter { !it.hasAnnotation<ExcludeAutoScan>() }
            .toList()

        serializableComponents.registerComponentSerializers(scanned)

        if (logger.config.minSeverity <= Severity.Verbose)
            logger.i("Autoscan found components: ${scanned.joinToString { it.simpleName!! }} in packages $limitToPackages")
        else logger.i("Autoscan found ${scanned.size} components in packages $limitToPackages")
    }

    /**
     * Registers any systems (including event listeners) that are annotated with [AutoScanner].
     *
     * Supports singletons or classes with no constructor parameters.
     *
     * @see AutoScanner
     */
//    fun systems() {
//        val scanned = reflections
//            .get(Scanners.MethodsAnnotated.with(AutoScan::class.java))
//            .mapNotNull { reflections.forMethod(it, classLoader)?.kotlinFunction }
//            .filter { it.parameters.singleOrNull()?.type == typeOf<GearyModule>() }
//
//        autoScanner.scannedSystems += scanned
//    }


    /** Registers a polymorphic serializer for this [kClass], scanning for any subclasses. */
    @OptIn(InternalSerializationApi::class)
    fun <T : Any> subClassesOf(kClass: KClass<T>) {
        val scanned = reflections
            .get(Scanners.SubTypes.of(kClass.java).asClass<Class<*>>(classLoader))
            .asSequence()
            .map { it.kotlin }
            .filter { !it.hasAnnotation<ExcludeAutoScan>() }
            .filterIsInstance<KClass<T>>()
            .toList()

        serializableComponents.formats.addModule {
            polymorphic(kClass) {
                scanned.forEach { scannedClass ->
                    runCatching { subclass(scannedClass, scannedClass.serializer()) }
                        .onFailure { logger.w("Failed to load subclass ${scannedClass.simpleName} of ${kClass.simpleName}") }
                }
                if (logger.config.minSeverity <= Severity.Verbose)
                    logger.i("Autoscan found subclasses for ${kClass.simpleName}: ${scanned.joinToString { it.simpleName!! }}")
                else logger.i("Autoscan found ${scanned.size} subclasses for ${kClass.simpleName}")
            }
        }
    }


    /** @see subClassesOf */
    inline fun <reified T : Any> subClassesOf() = subClassesOf(T::class)
}