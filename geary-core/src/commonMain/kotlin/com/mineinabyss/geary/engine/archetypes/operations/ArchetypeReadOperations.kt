package com.mineinabyss.geary.engine.archetypes.operations

import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.engine.EntityReadOperations
import com.mineinabyss.geary.modules.archetypes
import com.mineinabyss.geary.systems.accessors.RelationWithData

class ArchetypeReadOperations : EntityReadOperations {
    private val records get() = archetypes.records

    override fun getComponentFor(entity: Entity, componentId: ComponentId): Component? {
        records.runOn(entity) { archetype, row ->
            return archetype[row, componentId.let { if (it.hasRole(RELATION)) it else it.withRole(HOLDS_DATA) }]
        }
    }

    override fun getComponentsFor(entity: Entity): Array<Component> {
        records.runOn(entity) { archetype, row ->
            return archetype.getComponents(row).also { array ->
                archetype.relationsWithData.forEach { relation ->
                    val i = archetype.indexOf(relation)
                    array[i] = RelationWithData(array[i], null, Relation.of(relation))
                }
            }
        }
    }

    override fun exists(entity: Entity): Boolean {
        return records.contains(entity)
    }

    override fun getRelationsWithDataFor(
        entity: Entity,
        kind: ComponentId,
        target: EntityId,
    ): List<RelationWithData<*, *>> = records.runOn(entity) { archetype, row ->
        return archetype.readRelationDataFor(row, kind, target, archetype.getRelations(kind, target))
    }

    override fun getRelationsFor(entity: Entity, kind: ComponentId, target: EntityId): List<Relation> =
        records.runOn(entity) { archetype, _ -> archetype.getRelations(kind, target) }

    override fun hasComponentFor(entity: Entity, componentId: ComponentId): Boolean =
        records.runOn(entity) { archetype, _ -> componentId in archetype }
}
