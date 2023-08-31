package com.mineinabyss.geary.prefabs.systems

import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.components.relations.NoInherit
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.prefabs
import com.mineinabyss.geary.systems.Listener


class TrackPrefabsByKeySystem : Listener() {
    private val TargetScope.key by onSet<PrefabKey>().onTarget()

    @Handler
    private fun TargetScope.registerOnSet() {
        prefabs.manager.registerPrefab(key, entity)
        entity.addRelation<NoInherit, PrefabKey>()
    }
}
