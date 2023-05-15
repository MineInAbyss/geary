package com.mineinabyss.geary.modules

interface GearyModuleProvider<T : GearyModule> {
    fun init(module: T)
    fun start(module: T)
}
