package com.mineinabyss.geary.prefabs.configuration.systems

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.accessors.RelationWithData
import com.mineinabyss.geary.systems.builders.listener
import com.mineinabyss.geary.systems.query.ListenerQuery

@OptIn(UnsafeAccessors::class)
fun createParseRelationWithDataListener() = geary.listener(object : ListenerQuery() {
    val relationWithData by get<RelationWithData<*, *>>()
    override fun ensure() = event.anySet(::relationWithData)
}).exec {
    val entity = entity
    val data = relationWithData.data
    val targetData = relationWithData.targetData
    if (data != null) entity.set(data, relationWithData.relation.id)
    else entity.add(relationWithData.relation.id)
    if (targetData != null) entity.set(targetData, relationWithData.target.id)
    entity.remove<RelationWithData<*, *>>()
}
