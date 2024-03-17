package com.mineinabyss.geary.prefabs.configuration.systems

import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.prefabs.configuration.components.RelationOnPrefab
import com.mineinabyss.geary.systems.builders.listener
import com.mineinabyss.geary.systems.query.ListenerQuery

fun GearyModule.createParseRelationOnPrefabListener() = listener(object : ListenerQuery() {
    val relation by get<RelationOnPrefab>()
    override fun ensure() = event.anySet(::relation)
}).exec {
    try {
        val target = entity.lookup(relation.target)?.id ?: return@exec
        entity.setRelation(componentId(relation.data::class), target, relation.data)
    } finally {
        entity.remove<RelationOnPrefab>()
    }
}
