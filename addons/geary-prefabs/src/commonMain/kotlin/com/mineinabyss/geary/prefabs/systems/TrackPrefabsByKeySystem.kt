package com.mineinabyss.geary.prefabs.systems

import com.mineinabyss.geary.components.relations.NoInherit
import com.mineinabyss.geary.events.types.OnSet
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.prefabs
import com.mineinabyss.geary.systems.accessors.RelationWithData
import com.mineinabyss.geary.systems.builders.observe
import com.mineinabyss.geary.systems.query.Query

fun GearyModule.createTrackPrefabsByKeyListener() = observe<OnSet>()
    .involving<PrefabKey>()
    .exec { (key) ->
        prefabs.manager.registerPrefab(key, entity)
        entity.addRelation<NoInherit, PrefabKey>()
    }
