package com.mineinabyss.geary.addons.dsl

import org.koin.dsl.ScopeDSL
import kotlin.reflect.KClass

@GearyDSL
class AddonBuilder(
    val name: String,
) {
    private var createScope: ScopeDSL.() -> Unit = {}
    private val dependencies = mutableListOf<GearyAddon<*>>()
    private val onEnable = mutableListOf<AddonScope.() -> Unit>()
    private val onClose = mutableListOf<AddonScope.() -> Unit>()

    fun dependsOn(vararg addons: GearyAddon<*>) {
        dependencies += addons
    }

    fun scopedModule(block: @GearyDSL ScopeDSL.() -> Unit) {
        createScope = block
    }

    fun onEnable(block: AddonScope.() -> Unit) {
        onEnable += block
    }

    fun onClose(block: AddonScope.() -> Unit) {
        onClose += block
    }

    fun <T : Any> build(
        type: KClass<T>,
    ): GearyAddon<T> = GearyAddon(
        name = name,
        type = type,
        dependencies = dependencies.toList(),
        createScope = createScope,
        onInstall = this@AddonBuilder.onEnable.toList(),
        onUninstall = { this@AddonBuilder.onClose.forEach { it() } }
    )
}