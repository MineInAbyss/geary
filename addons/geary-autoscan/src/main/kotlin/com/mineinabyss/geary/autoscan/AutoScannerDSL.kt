package com.mineinabyss.geary.autoscan

import co.touchlab.kermit.Severity
import com.mineinabyss.geary.addons.dsl.GearyDSL
import com.mineinabyss.geary.datatypes.Component
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.modules.GearySetup
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.serialization.dsl.serialization
import kotlinx.serialization.*
import kotlinx.serialization.modules.polymorphic
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.FilterBuilder
import kotlin.reflect.KClass
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.kotlinFunction
import kotlin.reflect.typeOf

@GearyDSL
fun GearySetup.autoscan(
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
            .toList()

        geary {
            serialization {
                components {
                    scanned.forEach { scannedComponent ->
                        runCatching { component(scannedComponent) }
                            .onFailure {
                                when {
                                    geary.logger.config.minSeverity <= Severity.Verbose -> geary.logger.w("Failed to register component ${scannedComponent.simpleName}\n${it.stackTraceToString()}")
                                    else -> geary.logger.w("Failed to register component ${scannedComponent.simpleName} ${it::class.simpleName}: ${it.message}")
                                }
                            }
                    }
                }
            }
        }

        if (logger.config.minSeverity <= Severity.Verbose)
            logger.i("Autoscan found components: ${scanned.joinToString { it.simpleName!! }} in packages $limitTo")
        else logger.i("Autoscan found ${scanned.size} components in packages $limitTo")

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
            .get(Scanners.MethodsAnnotated.with(AutoScan::class.java))
            .mapNotNull { reflections.forMethod(it, classLoader)?.kotlinFunction }
            .filter { it.parameters.singleOrNull()?.type == typeOf<GearyModule>() }

        autoScanner.scannedSystems += scanned
    }


    /** Registers a polymorphic serializer for this [kClass], scanning for any subclasses. */
    @OptIn(InternalSerializationApi::class)
    fun <T : Any> subClassesOf(kClass: KClass<T>) {
        geary {
            serialization {
                application {
                    polymorphic(kClass) {
                        val scanned = this@AutoScannerDSL.reflections
                            .get(Scanners.SubTypes.of(kClass.java).asClass<Class<*>>(this@AutoScannerDSL.classLoader))
                            .asSequence()
                            .map { it.kotlin }
                            .filter { !it.hasAnnotation<ExcludeAutoScan>() }
                            .filterIsInstance<KClass<T>>()
                            .toList()

                        scanned.forEach { scannedClass ->
                            runCatching { subclass(scannedClass, scannedClass.serializer()) }
                                .onFailure { this@AutoScannerDSL.logger.w("Failed to load subclass ${scannedClass.simpleName} of ${kClass.simpleName}") }
                        }
                        if (geary.logger.config.minSeverity <= Severity.Verbose)
                            geary.logger.i("Autoscan found subclasses for ${kClass.simpleName}: ${scanned.joinToString { it.simpleName!! }}")
                        else geary.logger.i("Autoscan found ${scanned.size} subclasses for ${kClass.simpleName}")
                    }
                }
            }
        }

    }

    /** @see subClassesOf */
    inline fun <reified T : Any> subClassesOf() = subClassesOf(T::class)
}
