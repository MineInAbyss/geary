package com.mineinabyss.geary.prefabs.configuration.systems

import com.mineinabyss.geary.annotations.AutoScan
import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.RelationWithData
import com.mineinabyss.geary.systems.accessors.TargetScope

@AutoScan
class ParseRelationWithDataSystem : GearyListener() {
    private val TargetScope.relationWithData by onSet<RelationWithData<*, *>>()

    @Handler
    private fun TargetScope.convertToRelation() {
        val data = relationWithData.data
        val targetData = relationWithData.targetData
        if (data != null) entity.set(data, relationWithData.relation.id)
        else entity.add(relationWithData.relation.id)
        if (targetData != null) entity.set(targetData, relationWithData.target.id)
        entity.remove<RelationWithData<*, *>>()
    }
}
