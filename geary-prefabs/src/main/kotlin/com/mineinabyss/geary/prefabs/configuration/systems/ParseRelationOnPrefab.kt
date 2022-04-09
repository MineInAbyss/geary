package com.mineinabyss.geary.prefabs.configuration.systems

import com.mineinabyss.geary.autoscan.AutoScan
import com.mineinabyss.geary.ecs.accessors.TargetScope
import com.mineinabyss.geary.ecs.api.annotations.Handler
import com.mineinabyss.geary.ecs.api.systems.GearyListener
import com.mineinabyss.geary.ecs.api.systems.provideDelegate
import com.mineinabyss.geary.ecs.serialization.parseEntity
import com.mineinabyss.geary.prefabs.configuration.components.RelationOnPrefab

@AutoScan
public class ParseRelationOnPrefab : GearyListener() {
    private val TargetScope.relation by added<RelationOnPrefab>()

    @Handler
    private fun TargetScope.convertToRelation() {
        try {
            entity.setRelation(entity.parseEntity(relation.key).id, relation.value)
        } finally {
            entity.remove<RelationOnPrefab>()
        }
    }
}

