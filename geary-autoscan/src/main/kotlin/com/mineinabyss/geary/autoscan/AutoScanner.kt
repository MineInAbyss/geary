package com.mineinabyss.geary.autoscan

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
            private val logger = geary.logger
            override val scannedComponents = mutableSetOf<KClass<*>>()
            override val scannedSystems = mutableSetOf<KClass<*>>()

            override fun installSystems() {
                scannedSystems.asSequence()
                    .mapNotNull { it.objectInstance ?: runCatching { it.createInstance() }.getOrNull() }
                    .filterIsInstance<System>()
                    .onEach { geary.pipeline.addSystem(it) }
                    .map { it::class.simpleName }
                    .joinToString()
                    .let { logger.i("Autoscan loaded singleton systems: $it") }
            }
        }

        override fun AutoScanner.install() {
            DI.add(this)
            geary.pipeline.intercept(GearyPhase.INIT_SYSTEMS) {
                installSystems()
            }
        }
    }
}
