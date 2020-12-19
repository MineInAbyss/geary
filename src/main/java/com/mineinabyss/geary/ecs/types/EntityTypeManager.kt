package com.mineinabyss.geary.ecs.types

import org.bukkit.plugin.Plugin

public object EntityTypeManager {
    private val typeMap = hashMapOf<String, GearyEntityTypes<*>>()

    internal fun add(key: Plugin, types: GearyEntityTypes<*>) {
        typeMap[key.name] = types
    }

    public operator fun get(plugin: String): GearyEntityTypes<*>? = typeMap[plugin]

    public operator fun get(plugin: String, type: String): GearyEntityType? = get(plugin)?.get(type)
}
