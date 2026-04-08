package com.mineinabyss.geary.addons

import com.mineinabyss.dependencies.DI.Module
import com.mineinabyss.dependencies.MutableDIContext
import com.mineinabyss.dependencies.addCloseable
import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.MutableWorldScoped

fun gearyAddon(
    name: String,
    block: MutableWorldScoped.() -> Unit,
): Module = module(name) {
    val world = get<Geary>()
    val worldScoped = object : MutableWorldScoped {
        override val world: Geary = world
        override val di: MutableDIContext = this@module.di
    }
    addCloseable(worldScoped)
    worldScoped.apply { block() }
}