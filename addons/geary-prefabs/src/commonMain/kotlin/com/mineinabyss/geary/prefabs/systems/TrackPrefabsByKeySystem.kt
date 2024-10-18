package com.mineinabyss.geary.prefabs.systems

import com.mineinabyss.geary.components.relations.NoInherit
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.observe
import com.mineinabyss.geary.observers.Observer
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.Prefabs
import com.mineinabyss.geary.systems.query.query

fun Geary.createTrackPrefabsByKeyListener(): Observer = observe<OnSet>()
    .involving(query<PrefabKey>()).exec { (key) ->
        getAddon(Prefabs).manager.registerPrefab(key, entity)
        entity.addRelation<NoInherit, PrefabKey>()
    }
