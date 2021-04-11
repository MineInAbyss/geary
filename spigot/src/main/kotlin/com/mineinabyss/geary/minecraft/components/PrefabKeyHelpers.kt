package com.mineinabyss.geary.minecraft.components

import com.mineinabyss.geary.ecs.prefab.PrefabKey
import org.bukkit.plugin.Plugin

public fun PrefabKey.Companion.of(plugin: Plugin, name: String): PrefabKey =
    PrefabKey(plugin.name.toLowerCase(), name)
