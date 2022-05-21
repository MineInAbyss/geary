package com.mineinabyss.geary.prefabs.configuration.systems

import com.mineinabyss.geary.annotations.AutoScan
import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.prefabs.configuration.components.RelationOnPrefab
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.TargetScope


@AutoScan
class ParseRelationOnPrefab : GearyListener() {
    private val TargetScope.relation by added<RelationOnPrefab>()

    @Handler
    private fun TargetScope.convertToRelation() {
        try {
            val rel: RelationOnPrefab = relation
//            entity.setRelation(relation.value, entity.parseEntity(relation.key).id)
        } finally {
            entity.remove<RelationOnPrefab>()
        }
    }
}

