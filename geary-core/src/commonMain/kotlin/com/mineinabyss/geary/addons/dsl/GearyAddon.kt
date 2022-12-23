package com.mineinabyss.geary.addons.dsl

interface GearyAddonWithDefault<Module>: GearyAddon<Module> {
    fun default(): Module
}

interface GearyAddon<Module> {
    fun Module.install()
}
