package com.mineinabyss.geary.autoscan

import com.mineinabyss.geary.addons.dsl.GearyDSL
import com.mineinabyss.geary.datatypes.Component
import com.mineinabyss.geary.modules.GearyConfiguration
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.serialization.dsl.serialization
import com.mineinabyss.geary.systems.System
import kotlinx.serialization.*
import kotlinx.serialization.modules.polymorphic
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.FilterBuilder
import kotlin.reflect.KClass
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf

@GearyDSL
fun GearyConfiguration.autoscan(
    classLoader: ClassLoader,
    vararg limitToPackages: String,
    configure: AutoScannerDSL.() -> Unit
) =
    install(AutoScanner).also { AutoScannerDSL(classLoader, limitToPackages.toList()).configure() }

@GearyDSL
class AutoScannerDSL(
    private val classLoader: ClassLoader,
    private val limitTo: List<String>
) {
    private val logger get() = geary.logger

    private val reflections: Reflections by lazy {
        Reflections(
            ConfigurationBuilder()
                .apply { limitTo.forEach { forPackage(it, classLoader) } }
                .filterInputsBy(FilterBuilder().apply { limitTo.forEach { includePackage(it) } })
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
     * Registers serializers for [Component]s.
     *
     * @see AutoScanner
     */
    fun components() {
        val scanned = reflections
            .get(Scanners.TypesAnnotated.with(Serializable::class.java).asClass<Class<*>>(classLoader))
            .asSequence()
            .map { it.kotlin }
            .filter { !it.hasAnnotation<ExcludeAutoScan>() }

        geary {
            serialization {
                components {
                    scanned.forEach { scannedComponent ->
                        runCatching { component(scannedComponent) }
                            .onFailure {
                                if (it is ClassNotFoundException)
                                    this@AutoScannerDSL.logger.w("Failed to register component ${scannedComponent.simpleName}, class not found")
                                else
                                    this@AutoScannerDSL.logger.w("Failed to register component ${scannedComponent.simpleName}\n${it.stackTraceToString()}")
                            }
                    }
                }
            }
        }

        logger.i("Autoscan found components: ${scanned.joinToString { it.simpleName!! }}")

        autoScanner.scannedComponents += scanned
    }

    /**
     * Registers any systems (including event listeners) that are annotated with [AutoScanner].
     *
     * Supports singletons or classes with no constructor parameters.
     *
     * @see AutoScanner
     */
    fun systems() {
        val scanned = reflections
            .get(Scanners.TypesAnnotated.with(AutoScan::class.java).asClass<Class<*>>(classLoader))
            .asSequence()
            .map { it.kotlin }
            .filter { !it.hasAnnotation<ExcludeAutoScan>() && it.isSubclassOf(System::class) }

        autoScanner.scannedSystems += scanned
    }


    /** Registers a polymorphic serializer for this [kClass], scanning for any subclasses. */
    @OptIn(InternalSerializationApi::class)
    fun <T : Any> subClassesOf(kClass: KClass<T>) {
        geary {
            serialization {
                module {
                    polymorphic(kClass) {
                        val scanned = this@AutoScannerDSL.reflections
                            .get(Scanners.SubTypes.of(kClass.java).asClass<Class<*>>(this@AutoScannerDSL.classLoader))
                            .asSequence()
                            .map { it.kotlin }
                            .filter { !it.hasAnnotation<ExcludeAutoScan>() }
                            .filterIsInstance<KClass<T>>()

                        scanned.forEach { scannedClass ->
                            runCatching { subclass(scannedClass, scannedClass.serializer()) }
                                .onFailure { this@AutoScannerDSL.logger.w("Failed to load subclass ${scannedClass.simpleName} of ${kClass.simpleName}") }
                        }
                        this@AutoScannerDSL.logger.i("Autoscan found subclasses for ${kClass.simpleName}: ${scanned.joinToString { it.simpleName!! }}")
                    }
                }
            }
        }

    }

    /** @see subClassesOf */
    inline fun <reified T : Any> subClassesOf() = subClassesOf(T::class)
}
