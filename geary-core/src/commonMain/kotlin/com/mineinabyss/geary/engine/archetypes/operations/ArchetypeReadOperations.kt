package com.mineinabyss.geary.engine.archetypes.operations

import com.mineinabyss.geary.context.archetypes
import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.datatypes.maps.TypeMap
import com.mineinabyss.geary.engine.EntityReadOperations
import com.mineinabyss.geary.systems.accessors.RelationWithData

class ArchetypeReadOperations : EntityReadOperations {
    private val records: TypeMap get() = archetypes.records

    override fun getComponentFor(entity: Entity, componentId: ComponentId): Component? {
        val (archetype, row) = records[entity]
        return archetype[row, componentId.let { if (it.hasRole(RELATION)) it else it.withRole(HOLDS_DATA) }]
    }

    override fun getComponentsFor(entity: Entity): Array<Component> {
        val (archetype, row) = records[entity]
        return archetype.getComponents(row).also { array ->
            for (relation in archetype.relationsWithData) {
                val i = archetype.indexOf(relation.id)
                array[i] = RelationWithData(array[i], null, relation)
            }
        }
    }

    override fun getRelationsWithDataFor(
        entity: Entity,
        kind: ComponentId,
        target: EntityId,
    ): List<RelationWithData<*, *>> {
        val (arc, row) = records[entity]
        return arc.getRelations(kind, target).map { relation ->
            RelationWithData(
                data = if (kind.hasRole(HOLDS_DATA)) arc[row, relation.id] else null,
                targetData = if (target.hasRole(HOLDS_DATA)) arc[row, relation.target.withRole(HOLDS_DATA)] else null,
                relation = relation
            )
        }
    }

    override fun getRelationsFor(entity: Entity, kind: ComponentId, target: EntityId): List<Relation> =
        records[entity].archetype.getRelations(kind, target)

    override fun hasComponentFor(entity: Entity, componentId: ComponentId): Boolean =
        componentId in records[entity].archetype
}
