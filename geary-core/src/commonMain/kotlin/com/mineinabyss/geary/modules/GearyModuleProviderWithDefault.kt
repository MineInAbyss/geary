package com.mineinabyss.geary.modules

interface GearyModuleProviderWithDefault<T : GearyModule> : GearyModuleProvider<T> {
    fun default(): T
}
