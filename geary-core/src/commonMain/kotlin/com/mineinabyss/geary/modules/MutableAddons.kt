package com.mineinabyss.geary.modules

import com.mineinabyss.geary.addons.dsl.Addon

class MutableAddons {
    data class AddonToConfig<T>(val addon: Addon<T, *>, val config: T)

    @PublishedApi
    internal val addons = mutableMapOf<String, AddonToConfig<*>>()
    internal val instances = mutableMapOf<String, Any>()
    internal val addonsOrder = mutableListOf<AddonToConfig<*>>()

    fun <I> getInstance(addon: Addon<*, I>): I? {
        return instances[addon.name] as? I
    }

    fun <T> getOrPut(setup: GearySetup, addon: Addon<T, *>, customConfiguration: (() -> T)? = null): AddonToConfig<T> {
        return addons.getOrPut(addon.name) {
            AddonToConfig(addon, customConfiguration?.invoke() ?: addon.defaultConfiguration(setup))
                .also { addonsOrder.add(it) }
        } as AddonToConfig<T>
    }

    fun <T> getConfig(addon: Addon<T, *>): T {
        return addons[addon.name]?.config as? T ?: error("Config for addon ${addon.name} not found")
    }

    fun init(addon: AddonToConfig<*>, setup: GearySetup) {
        instances[addon.addon.name] = (addon.addon.onInstall as GearySetup.(Any?) -> Any).invoke(setup, addon.config)
    }

    fun initAll(setup: GearySetup) {
        addonsOrder.forEach { addon ->
            init(addon, setup)
        }
    }
}
