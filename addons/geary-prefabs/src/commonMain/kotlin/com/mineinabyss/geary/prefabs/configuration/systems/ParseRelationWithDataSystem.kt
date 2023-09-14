package com.mineinabyss.geary.prefabs.configuration.systems

import com.mineinabyss.geary.datatypes.Records
import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.systems.Listener
import com.mineinabyss.geary.systems.accessors.Pointers
import com.mineinabyss.geary.systems.accessors.RelationWithData


class ParseRelationWithDataSystem : Listener() {
    private val Records.relationWithData by get<RelationWithData<*, *>>().whenSetOnTarget()

    @OptIn(UnsafeAccessors::class)
    override fun Pointers.handle() {
        val entity = target.entity
        val data = relationWithData.data
        val targetData = relationWithData.targetData
        if (data != null) entity.set(data, relationWithData.relation.id)
        else entity.add(relationWithData.relation.id)
        if (targetData != null) entity.set(targetData, relationWithData.target.id)
        entity.remove<RelationWithData<*, *>>()
    }
}
