package com.mineinabyss.geary.prefabs.systems

import com.mineinabyss.geary.components.relations.NoInherit
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.prefabs
import com.mineinabyss.geary.systems.builders.observe
import com.mineinabyss.geary.systems.query.query

fun GearyModule.createTrackPrefabsByKeyListener() = observe<OnSet>()
    .involving(query<PrefabKey>()).exec { (key) ->
        prefabs.manager.registerPrefab(key, entity)
        entity.addRelation<NoInherit, PrefabKey>()
    }
