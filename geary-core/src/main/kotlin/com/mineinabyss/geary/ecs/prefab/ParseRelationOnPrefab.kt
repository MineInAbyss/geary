package com.mineinabyss.geary.ecs.prefab

import com.mineinabyss.geary.ecs.accessors.TargetScope
import com.mineinabyss.geary.ecs.accessors.get
import com.mineinabyss.geary.ecs.api.autoscan.AutoScan
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.systems.GearyListener
import com.mineinabyss.geary.ecs.api.systems.Handler
import com.mineinabyss.geary.ecs.components.RelationComponent
import com.mineinabyss.geary.ecs.components.RelationOnPrefab

@AutoScan
public class ParseRelationOnPrefab : GearyListener() {
    private val TargetScope.relation by get<RelationOnPrefab>()

    init {
        allAdded()
    }

    @Handler
    private fun TargetScope.convertToRelation() {
        entity.setRelation(componentId(relation.key), relation.value)
        entity.remove<RelationOnPrefab>()
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
