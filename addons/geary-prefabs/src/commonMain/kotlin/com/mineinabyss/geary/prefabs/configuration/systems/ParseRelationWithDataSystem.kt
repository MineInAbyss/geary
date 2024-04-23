package com.mineinabyss.geary.prefabs.configuration.systems

import com.mineinabyss.geary.events.types.OnSet
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.prefabs.configuration.components.RelationOnPrefab
import com.mineinabyss.geary.systems.accessors.RelationWithData
import com.mineinabyss.geary.systems.builders.observe
import com.mineinabyss.geary.systems.query.Query
import com.mineinabyss.geary.systems.query.query

fun GearyModule.createParseRelationWithDataListener() = observe<OnSet>()
    .involving(query<RelationWithData<*, *>>()).exec { (relationWithData) ->
        val entity = entity
        val data = relationWithData.data
        val targetData = relationWithData.targetData
        if (data != null) entity.set(data, relationWithData.relation.id)
        else entity.add(relationWithData.relation.id)
        if (targetData != null) entity.set(targetData, relationWithData.target.id)
        entity.remove<RelationWithData<*, *>>()
    }
