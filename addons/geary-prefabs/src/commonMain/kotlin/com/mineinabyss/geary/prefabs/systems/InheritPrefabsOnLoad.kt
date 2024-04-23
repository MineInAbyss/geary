package com.mineinabyss.geary.prefabs.systems

import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.prefabs.events.PrefabLoaded
import com.mineinabyss.geary.prefabs.helpers.inheritPrefabsIfNeeded
import com.mineinabyss.geary.systems.builders.observe

fun GearyModule.createInheritPrefabsOnLoadListener() = observe<PrefabLoaded>()
    .exec { entity.inheritPrefabsIfNeeded() }
