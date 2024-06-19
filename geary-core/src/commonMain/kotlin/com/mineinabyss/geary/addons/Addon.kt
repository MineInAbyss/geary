package com.mineinabyss.geary.addons

import com.mineinabyss.geary.modules.GearyModule

interface Addon<TApplication: Application, TConfiguration : Any, TPlugin : Any> {
    fun install(app: TApplication, configure: TConfiguration.() -> Unit): TPlugin
}

interface ApplicationFactory<TApplication: Application, TConfiguration : Any> {
    fun create(configure: TConfiguration.() -> Unit): TApplication

}
interface AddonBuilder<TApplication: Application, TConfig: Any> {
    val application: TApplication
    val config: TConfig
}

inline fun <TApplication: Application, TConfig: Any> createAddon(
    crossinline createConfiguration: () -> TConfig,
    crossinline body: AddonBuilder<TApplication, TConfig>.() -> Unit,
) = object : Addon<TApplication, TConfig, Unit> {
    override fun install(app: TApplication, configure: TConfig.() -> Unit) {
        val config = createConfiguration().apply(configure)
        val builder = object : AddonBuilder<TApplication, TConfig> {
            override val application = app
            override val config = config
        }
        body(builder)
    }
}

inline fun <TConfig: Any> createGearyAddon(
    crossinline createConfiguration: () -> TConfig,
    crossinline body: AddonBuilder<GearyModule, TConfig>.() -> Unit,
) = createAddon(createConfiguration, body)
