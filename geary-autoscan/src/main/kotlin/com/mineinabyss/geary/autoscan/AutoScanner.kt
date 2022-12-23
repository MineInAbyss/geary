package com.mineinabyss.geary.autoscan

import com.mineinabyss.ding.DI
import com.mineinabyss.geary.addons.GearyPhase
import com.mineinabyss.geary.addons.Namespaced
import com.mineinabyss.geary.addons.dsl.GearyAddon
import com.mineinabyss.geary.addons.dsl.GearyAddonWithDefault
import com.mineinabyss.geary.addons.dsl.GearyDSL
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.System
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

val autoScanner by DI.observe<AutoScanner>()

interface AutoScanner {
    val scannedComponents: MutableSet<KClass<*>>
    val scannedSystems: MutableSet<KClass<*>>

    fun installSystems()

    companion object Addon : GearyAddonWithDefault<AutoScanner> {
        override fun default() = object : AutoScanner {
            private val logger = geary.logger
            override val scannedComponents = mutableSetOf<KClass<*>>()
            override val scannedSystems = mutableSetOf<KClass<*>>()

            override fun installSystems() {
                scannedSystems.asSequence()
                    .mapNotNull { it.objectInstance ?: runCatching { it.createInstance() }.getOrNull() }
                    .filterIsInstance<System>()
                    .onEach { geary.systems.add(it) }
                    .map { it::class.simpleName }
                    .joinToString()
                    .let { logger.i("Autoscan loaded singleton systems: $it") }
            }
        }

        override fun AutoScanner.install() {
            geary.pipeline.intercept(GearyPhase.INIT_SYSTEMS) {
                installSystems()
            }
        }
    }
}

@GearyDSL
fun Namespaced.autoscan(vararg limitToPackages: String, configure: AutoScannerDSL.() -> Unit) =
    gearyConf.install(AutoScanner).also { AutoScannerDSL(this, limitToPackages.toList()).configure() }
