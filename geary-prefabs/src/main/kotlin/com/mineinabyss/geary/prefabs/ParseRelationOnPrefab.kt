package com.mineinabyss.geary.prefabs

import com.mineinabyss.geary.ecs.accessors.TargetScope
import com.mineinabyss.geary.ecs.accessors.building.get
import com.mineinabyss.geary.ecs.api.autoscan.AutoScan
import com.mineinabyss.geary.ecs.api.autoscan.Handler
import com.mineinabyss.geary.ecs.api.systems.GearyListener
import com.mineinabyss.geary.ecs.components.RelationComponent
import com.mineinabyss.geary.ecs.serialization.parseEntity
import com.mineinabyss.geary.prefabs.configuration.components.RelationOnPrefab

@AutoScan
public class ParseRelationOnPrefab : GearyListener() {
    private val TargetScope.relation by get<RelationOnPrefab>()

    init {
        allAdded()
    }

    @Handler
    private fun TargetScope.convertToRelation() {
        try {
            entity.setRelation(entity.parseEntity(relation.key).id, relation.value)
        } finally {
            entity.remove<RelationOnPrefab>()
        }
    }
}

@AutoScan
public class ParseRelationComponent : GearyListener() {
    private val TargetScope.relation by get<RelationComponent>()

    init {
        allAdded()
    }

    @Handler
    private fun TargetScope.convertToRelation() {
        entity.setRelation(relation.key, relation.value)
        entity.remove<RelationComponent>()
    }
}
