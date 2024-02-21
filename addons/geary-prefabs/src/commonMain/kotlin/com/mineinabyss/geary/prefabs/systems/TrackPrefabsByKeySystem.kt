package com.mineinabyss.geary.prefabs.systems

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.components.relations.NoInherit
import com.mineinabyss.geary.datatypes.Records
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.prefabs
import com.mineinabyss.geary.systems.Listener
import com.mineinabyss.geary.systems.accessors.Pointers


class TrackPrefabsByKeySystem : Listener() {
    private val Records.key by get<PrefabKey>().whenSetOnTarget()

    @OptIn(UnsafeAccessors::class)
    override fun Pointers.handle() {
        prefabs.manager.registerPrefab(key, target.entity)
        target.entity.addRelation<NoInherit, PrefabKey>()
    }
}
