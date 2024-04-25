package com.mineinabyss.geary.prefabs.configuration.systems

import com.mineinabyss.geary.events.types.OnSet
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.prefabs.configuration.components.RelationOnPrefab
import com.mineinabyss.geary.systems.builders.observe
import com.mineinabyss.geary.systems.query.query

fun GearyModule.createParseRelationOnPrefabListener() = observe<OnSet>()
    .involving(query<RelationOnPrefab>()).exec { (relation) ->
        try {
            val target = entity.lookup(relation.target)?.id ?: return@exec
            entity.setRelation(componentId(relation.data::class), target, relation.data)
        } finally {
            entity.remove<RelationOnPrefab>()
        }
    }
