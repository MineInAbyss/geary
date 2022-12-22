package com.mineinabyss.geary.papermc.helpers

import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.geary.prefabs.PrefabKey
import org.bukkit.entity.Entity

val Entity.customMobType: String
    //TODO decide whether we want NMS in this module
    get() = toGeary().get<PrefabKey>()?.toString() ?: /*toNMS().*/type.name
