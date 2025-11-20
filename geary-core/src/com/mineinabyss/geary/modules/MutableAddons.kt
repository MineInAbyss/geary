package com.mineinabyss.geary.modules

import com.mineinabyss.geary.addons.dsl.AddonScope
import com.mineinabyss.geary.addons.dsl.GearyAddon
import org.koin.core.Koin
import org.koin.core.qualifier.named
import org.koin.dsl.module

class MutableAddons(
    private val koin: Koin,
) {
    @PublishedApi
    internal val addons = mutableMapOf<String, GearyAddon<*>>()
    internal val createdScopes = mutableMapOf<GearyAddon<*>, AddonScope>()

    fun <T : Any> install(addon: GearyAddon<T>): T {
        if (addon in createdScopes) return getAddon(addon)

        addons[addon.name] = addon
        koin.loadModules(listOf(module {
            scope(named(addon.name)) {
                addon.createScope(this)
            }
        }))

        val scope = koin.createScope(addon.name, named(addon.name))
        val unmetDeps = addon.dependencies.filter { it !in createdScopes }
        if (unmetDeps.isNotEmpty()) {
            error("Addon ${addon.name} could not be installed, dependencies were not met: ${unmetDeps.joinToString()}")
        }
        addon.dependencies.forEach {
            scope.linkTo(createdScopes.getValue(it).scope)
        }
        val addonScope = AddonScope(scope)
        addon.onInstall(addonScope)
        createdScopes[addon] = AddonScope(scope)
        return getAddon(addon)
    }

    fun getScope(addon: GearyAddon<*>): AddonScope =
        createdScopes[addon] ?: error("Scope for addon ${addon.name} not found")

    fun <T : Any> getAddon(addon: GearyAddon<T>): T {
        val scope = getScope(addon)
        return if (addon.type != Unit::class) scope.scope.get(addon.type) else Unit as T
    }

    fun <T : Any> getAddonOrNull(addon: GearyAddon<T>): T? {
        if (addon !in createdScopes) return null
        return getAddon(addon)
    }

    fun <T : Any> uninstall(addon: GearyAddon<T>) {
        val scope = getScope(addon)
        addon.onUninstall.invoke(scope)
        addons -= addon.name
        createdScopes.remove(addon)?.close()
    }
}
