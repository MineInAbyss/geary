package com.mineinabyss.geary.prefabs.systems

import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.prefabs.events.PrefabLoaded
import com.mineinabyss.geary.prefabs.helpers.inheritPrefabs
import com.mineinabyss.geary.systems.builders.listener
import com.mineinabyss.geary.systems.query.ListenerQuery

fun GearyModule.createInheritPrefabsOnLoadListener() = listener(object : ListenerQuery() {
    override fun ensure() = event { has<PrefabLoaded>() }
}).exec { entity.inheritPrefabs() }
