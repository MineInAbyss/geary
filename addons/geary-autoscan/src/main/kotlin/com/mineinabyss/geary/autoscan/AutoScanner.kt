package com.mineinabyss.geary.autoscan

import co.touchlab.kermit.Severity
import com.mineinabyss.geary.addons.GearyPhase
import com.mineinabyss.geary.addons.dsl.GearyAddonWithDefault
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.System
import com.mineinabyss.idofront.di.DI
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

val autoScanner by DI.observe<AutoScanner>()

interface AutoScanner {
    val scannedComponents: MutableSet<KClass<*>>
    val scannedSystems: MutableSet<KClass<*>>

    fun installSystems()

    companion object Addon : GearyAddonWithDefault<AutoScanner> {
        override fun default() = object : AutoScanner {
            private val logger get() = geary.logger
            override val scannedComponents = mutableSetOf<KClass<*>>()
            override val scannedSystems = mutableSetOf<KClass<*>>()

            override fun installSystems() {
                scannedSystems.asSequence()
                    .mapNotNull { it.objectInstance ?: runCatching { it.createInstance() }.getOrNull() }
                    .filterIsInstance<System>()
                    .onEach { geary.pipeline.addSystem(it) }
                    .map { it::class.simpleName }
                    .let {
                        if (logger.config.minSeverity <= Severity.Verbose)
                            logger.i("Autoscan loaded singleton systems: ${it.joinToString()}")
                        else logger.i("Autoscan loaded ${it.count()} singleton systems")
                    }
            }
        }

        override fun AutoScanner.install() {
            geary.pipeline.runOnOrAfter(GearyPhase.INIT_SYSTEMS) {
                installSystems()
            }
        }
    }
}
