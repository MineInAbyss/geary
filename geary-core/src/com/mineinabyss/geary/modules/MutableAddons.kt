package com.mineinabyss.geary.modules

import co.touchlab.kermit.Logger
import com.mineinabyss.geary.addons.dsl.AddonScope
import com.mineinabyss.geary.addons.dsl.GearyAddon
import org.koin.core.Koin
import org.koin.core.error.InstanceCreationException
import org.koin.core.error.NoDefinitionFoundException
import org.koin.core.qualifier.named
import org.koin.dsl.module

class MutableAddons(
    private val koin: Koin,
) {
    @PublishedApi
    internal val addons = mutableMapOf<String, GearyAddon<*>>()
    internal val createdScopes = mutableMapOf<String, AddonScope>()

    fun <T : Any> install(addon: GearyAddon<T>): T {
        if (addon.name in createdScopes) return getAddon(addon)

        addons[addon.name] = addon
        koin.loadModules(listOf(module {
            scope(named(addon.name)) {
                addon.createScope(this)
            }
        }))

        try {
            val scope = koin.createScope(addon.name, named(addon.name))
            val unmetDeps = addon.dependencies.filter { it.name !in createdScopes }
            if (unmetDeps.isNotEmpty()) {
                error("Addon ${addon.name} could not be installed, dependencies were not met: ${unmetDeps.map { it.name }}")
            }
            addon.dependencies.forEach {
                scope.linkTo(createdScopes.getValue(it.name).scope)
            }
            val addonScope = AddonScope(addon.name, scope)
            addon.onInstall.forEach { it(addonScope) }
            createdScopes[addon.name] = AddonScope(addon.name, scope)
            return getAddon(addon)
        } catch (e: InstanceCreationException) {
            e.printCleanErrorMessage(koin.get<Logger>())
            error("Failed to install addon ${addon.name}")
        }
    }

    fun getScope(addon: GearyAddon<*>): AddonScope = getScope(addon.name)

    fun getScope(name: String): AddonScope = createdScopes[name] ?: error("Scope for addon $name not found")

    fun <T : Any> getAddon(addon: GearyAddon<T>): T {
        val scope = getScope(addon)
        return if (addon.type != Unit::class) scope.scope.get(addon.type) else Unit as T
    }

    fun <T : Any> getAddonOrNull(addon: GearyAddon<T>): T? {
        if (addon.name !in createdScopes) return null
        return getAddon(addon)
    }

    fun <T : Any> uninstall(addon: GearyAddon<T>) {
        val scope = getScope(addon)
        addon.onUninstall.invoke(scope)
        createdScopes.remove(addon.name)?.close()
        addons -= addon.name
        koin.deleteScope(addon.name)
    }

    private fun InstanceCreationException.printCleanErrorMessage(
        logger: Logger,
        first: Boolean = true,
    ) {
        if (first) logger.e { "$message" }
        else logger.e { "Caused by: $message" }

        when (val cause = cause) {
            is InstanceCreationException -> cause.printCleanErrorMessage(logger, false)
            is NoDefinitionFoundException -> logger.e { "Caused by: " + cause.message }
            else -> logger.e { "Caused by: " + cause?.stackTraceToString() }
        }
    }
}
