package com.mineinabyss.geary.prefabs.systems

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.components.relations.NoInherit
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.prefabs
import com.mineinabyss.geary.systems.builders.listener
import com.mineinabyss.geary.systems.query.ListenerQuery

@OptIn(UnsafeAccessors::class)
fun createTrackPrefabsByKeyListener() = geary.listener(object : ListenerQuery() {
    val key by get<PrefabKey>()
    override fun ensure() = event.anySet(::key)
}).exec {
    prefabs.manager.registerPrefab(key, entity)
    entity.addRelation<NoInherit, PrefabKey>()
}
