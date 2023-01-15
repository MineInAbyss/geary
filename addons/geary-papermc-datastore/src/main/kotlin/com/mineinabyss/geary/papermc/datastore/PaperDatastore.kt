package com.mineinabyss.geary.papermc.datastore

import com.mineinabyss.idofront.util.toMCKey
import org.bukkit.NamespacedKey

object PaperDatastore {
    val COMPONENTS_KEY: NamespacedKey = "geary:components".toMCKey()
    val PREFABS_KEY = "geary:prefabs".toMCKey()
}
