package com.mineinabyss.geary.papermc.components

import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.PrefabManager
import org.bukkit.plugin.Plugin

public fun PrefabKey.Companion.of(plugin: Plugin, name: String): PrefabKey =
    of(plugin.name.lowercase(), name)

public fun PrefabManager.getPrefabsFor(plugin: Plugin): List<PrefabKey> =
    getPrefabsFor(plugin.name.lowercase())
