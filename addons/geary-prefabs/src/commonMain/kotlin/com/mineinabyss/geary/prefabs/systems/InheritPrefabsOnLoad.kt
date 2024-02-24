package com.mineinabyss.geary.prefabs.systems

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.prefabs.events.PrefabLoaded
import com.mineinabyss.geary.prefabs.helpers.inheritPrefabs
import com.mineinabyss.geary.systems.listener
import com.mineinabyss.geary.systems.query.ListenerQuery

@OptIn(UnsafeAccessors::class)
fun createInheritPrefabsOnLoadListener() = geary.listener(object : ListenerQuery() {
    override fun ensure() = event { has<PrefabLoaded>() }
}).exec { entity.inheritPrefabs() }
