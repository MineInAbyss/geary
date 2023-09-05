package com.mineinabyss.geary.prefabs.configuration.systems

import com.mineinabyss.geary.datatypes.Records
import com.mineinabyss.geary.prefabs.configuration.components.RelationOnPrefab
import com.mineinabyss.geary.systems.Listener


class ParseRelationOnPrefab : Listener() {
    private var Records.relation by get<RelationOnPrefab>().removable().whenSetOnTarget()

    override fun Records.handle() {
        try {
            val rel: RelationOnPrefab = relation!!
//            entity.setRelation(relation.value, entity.parseEntity(relation.key).id)
        } finally {
            relation = null
        }
    }
}

