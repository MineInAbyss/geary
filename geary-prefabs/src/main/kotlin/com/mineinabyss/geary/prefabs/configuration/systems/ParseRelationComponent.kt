package com.mineinabyss.geary.prefabs.configuration.systems

import com.mineinabyss.geary.autoscan.AutoScan
import com.mineinabyss.geary.ecs.accessors.TargetScope
import com.mineinabyss.geary.ecs.api.annotations.Handler
import com.mineinabyss.geary.ecs.api.systems.GearyListener
import com.mineinabyss.geary.ecs.api.systems.provideDelegate
import com.mineinabyss.geary.ecs.components.RelationComponent

@AutoScan
public class ParseRelationComponent : GearyListener() {
    private val TargetScope.relation by added<RelationComponent>()

    @Handler
    private fun TargetScope.convertToRelation() {
        entity.setRelation(relation.key, relation.value)
        entity.remove<RelationComponent>()
    }
}
