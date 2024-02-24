package com.mineinabyss.geary.prefabs.configuration.systems

import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.prefabs.configuration.components.RelationOnPrefab
import com.mineinabyss.geary.systems.listener
import com.mineinabyss.geary.systems.query.ListenerQuery


fun createParseRelationOnPrefabListener() = geary.listener(object : ListenerQuery() {
    var relation by get<RelationOnPrefab>().removable()
    override fun ensure() = event.anySet(::relation)
}).exec {
    try {
        val rel: RelationOnPrefab = relation!!
//            entity.setRelation(relation.value, entity.parseEntity(relation.key).id)
    } finally {
        relation = null
    }
}
