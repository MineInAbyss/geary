package com.mineinabyss.geary.engine.archetypes.operations

import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.datatypes.maps.TypeMap
import com.mineinabyss.geary.engine.EntityReadOperations
import com.mineinabyss.geary.modules.archetypes
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
            archetype.relationsWithData.forEach { relation ->
                val i = archetype.indexOf(relation)
                array[i] = RelationWithData(array[i], null, Relation.of(relation))
            }
        }
    }

    override fun getRelationsWithDataFor(
        entity: Entity,
        kind: ComponentId,
        target: EntityId,
    ): List<RelationWithData<*, *>> {
        val (arc, row) = records[entity]

        return arc.readRelationDataFor(row, kind, target, arc.getRelations(kind, target))
    }

    override fun getRelationsFor(entity: Entity, kind: ComponentId, target: EntityId): List<Relation> =
        records[entity].archetype.getRelations(kind, target)

    override fun hasComponentFor(entity: Entity, componentId: ComponentId): Boolean =
        componentId in records[entity].archetype
}
