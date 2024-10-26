package com.mineinabyss.geary.prefabs.systems

import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.observe
import com.mineinabyss.geary.prefabs.events.PrefabLoaded
import com.mineinabyss.geary.prefabs.helpers.inheritPrefabsIfNeeded

fun Geary.createInheritPrefabsOnLoadListener() = observe<PrefabLoaded>()
    .exec { entity.inheritPrefabsIfNeeded() }
