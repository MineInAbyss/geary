package com.mineinabyss.geary.prefabs.configuration.systems

import com.mineinabyss.geary.prefabs.configuration.components.RelationOnPrefab
import com.mineinabyss.geary.systems.Listener
import com.mineinabyss.geary.systems.accessors.Pointers


class ParseRelationOnPrefab : Listener() {
    private var Pointers.relation by get<RelationOnPrefab>().removable().whenSetOnTarget()

    override fun Pointers.handle() {
        try {
            val rel: RelationOnPrefab = relation!!
//            entity.setRelation(relation.value, entity.parseEntity(relation.key).id)
        } finally {
            relation = null
        }
    }
}

