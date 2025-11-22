package com.mineinabyss.geary.addons.dsl

import org.koin.dsl.ScopeDSL
import kotlin.jvm.JvmName
import kotlin.reflect.KClass

data class GearyAddon<T : Any>(
    val name: String,
    val type: KClass<T>,
    val dependencies: List<GearyAddon<*>>,
    val createScope: ScopeDSL.() -> Unit,
    val onInstall: List<AddonScope.() -> Unit>,
    val onUninstall: AddonScope.() -> Unit,
) {
    fun overrideScope(block: ScopeDSL.() -> Unit): GearyAddon<T> {
        return copy(createScope = {
            createScope()
            block()
        })
    }
}

inline fun <reified T : Any> createAddon(
    name: String,
    type: KClass<T> = T::class,
    install: AddonBuilder.() -> Unit,
): GearyAddon<T> {
    return AddonBuilder(name).apply(install).build(type)
}

@JvmName("createAddonUnit")
inline fun createAddon(name: String, install: AddonBuilder.() -> Unit): GearyAddon<Unit> {
    return AddonBuilder(name).apply(install).build(Unit::class)
}