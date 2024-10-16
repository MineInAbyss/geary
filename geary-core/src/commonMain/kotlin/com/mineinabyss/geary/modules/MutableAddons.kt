package com.mineinabyss.geary.modules

import com.mineinabyss.geary.addons.dsl.Addon

class MutableAddons {
    data class AddonToConfig<T>(val addon: Addon<T, *>, val config: T)

    @PublishedApi
    internal val addons = mutableMapOf<String, AddonToConfig<*>>()

    fun <T> getOrPut(world: Geary, addon: Addon<T, *>, customConfiguration: (() -> T)? = null): AddonToConfig<T> {
        return addons.getOrPut(addon.name) {
            AddonToConfig(addon, customConfiguration?.invoke() ?: addon.defaultConfiguration(world))
        } as AddonToConfig<T>
    }

    fun initAll(setup: GearySetup) {
        addons.forEach { (name, addon) ->
            val geary = Geary(setup.application, setup.logger.withTag(name))
            (addon.addon.onInstall as Geary.(Any?) -> Any).invoke(geary, addon.config)
        }
    }
}
