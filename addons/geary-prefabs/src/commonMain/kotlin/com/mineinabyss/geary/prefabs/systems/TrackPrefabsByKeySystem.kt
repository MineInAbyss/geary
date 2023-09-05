package com.mineinabyss.geary.prefabs.systems

import com.mineinabyss.geary.components.relations.NoInherit
import com.mineinabyss.geary.datatypes.Records
import com.mineinabyss.geary.datatypes.UnsafeAccessors
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.prefabs
import com.mineinabyss.geary.systems.Listener


class TrackPrefabsByKeySystem : Listener() {
    private val Records.key by get<PrefabKey>().whenSetOnTarget()

    @OptIn(UnsafeAccessors::class)
    override fun Records.handle() {
        prefabs.manager.registerPrefab(key, target.entity)
        target.entity.addRelation<NoInherit, PrefabKey>()
    }
}
