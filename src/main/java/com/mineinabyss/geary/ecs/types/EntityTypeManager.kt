package com.mineinabyss.geary.ecs.types

import org.bukkit.plugin.Plugin

internal object EntityTypeManager {
    private val typeMap = hashMapOf<String, GearyEntityTypes<*>>()

    fun add(key: Plugin, types: GearyEntityTypes<*>) {
        typeMap[key.name] = types
    }

    operator fun get(plugin: String) = typeMap[plugin]

    operator fun get(plugin: String, type: String) = get(plugin)?.get(type)
}
