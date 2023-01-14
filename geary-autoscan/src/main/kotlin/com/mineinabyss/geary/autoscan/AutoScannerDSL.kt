package com.mineinabyss.geary.autoscan

import com.mineinabyss.geary.addons.Namespaced
import com.mineinabyss.geary.addons.dsl.GearyDSL
import com.mineinabyss.geary.datatypes.Component
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.serialization.dsl.serialization
import com.mineinabyss.geary.systems.System
import kotlinx.serialization.*
import org.reflections.Reflections
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf

@GearyDSL
fun Namespaced.autoscan(classLoader: ClassLoader, vararg limitToPackages: String, configure: AutoScannerDSL.() -> Unit) =
    gearyConf.install(AutoScanner).also { AutoScannerDSL(this, classLoader, limitToPackages.toList()).configure() }

@GearyDSL
class AutoScannerDSL(
    private val namespaced: Namespaced,
    private val classLoader: ClassLoader,
    private val limitTo: List<String>
) {
    private val logger = geary.logger

    private val reflections: Reflections by lazy {
        Reflections(
            ConfigurationBuilder()
                .addClassLoader(classLoader)
                .apply {
                    limitTo.forEach { pkg ->
                        addUrls(ClasspathHelper.forPackage(pkg, classLoader))
                    }
                }
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
            .getTypesAnnotatedWith(Serializable::class.java)
            .asSequence()
            .map { it.kotlin }
            .filter { !it.hasAnnotation<ExcludeAutoScan>() }

        geary {
            this@AutoScannerDSL.namespaced.serialization {
                components {
                    scanned.forEach { component(it) }
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
        val scanned = reflections.getTypesAnnotatedWith(AutoScan::class.java)
            .asSequence()
            .map { it.kotlin }
            .filter { !it.hasAnnotation<ExcludeAutoScan>() && it.isSubclassOf(System::class) }

        autoScanner.scannedSystems += scanned
    }
}
