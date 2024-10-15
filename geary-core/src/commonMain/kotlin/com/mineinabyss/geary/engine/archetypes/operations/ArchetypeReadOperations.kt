package com.mineinabyss.geary.engine.archetypes.operations

import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.datatypes.maps.ArrayTypeMap
import com.mineinabyss.geary.engine.ComponentProvider
import com.mineinabyss.geary.engine.Components
import com.mineinabyss.geary.engine.EntityReadOperations
import com.mineinabyss.geary.engine.id
import com.mineinabyss.geary.helpers.toGeary
import com.mineinabyss.geary.systems.accessors.RelationWithData

class ArchetypeReadOperations(
    val components: Components,
    val records: ArrayTypeMap,
) : EntityReadOperations {
    override fun get(entity: EntityId, componentId: ComponentId): Component? {
        records.runOn(entity) { archetype, row ->
            return archetype[row, componentId.let { if (it.hasRole(RELATION)) it else it.withRole(HOLDS_DATA) }]
        }
    }

    override fun getAll(entity: EntityId): Array<Component> {
        records.runOn(entity) { archetype, row ->
            return archetype.getComponents(row).also { array ->
                archetype.relationsWithData.forEach { relation ->
                    val i = archetype.indexOf(relation)
                    array[i] = RelationWithData(array[i], null, Relation.of(relation))
                }
            }
        }
    }

    override fun exists(entity: EntityId): Boolean {
        return records.contains(entity)
    }

    override fun getRelationsWithDataFor(
        entity: EntityId,
        kind: ComponentId,
        target: EntityId,
    ): List<RelationWithData<*, *>> = records.runOn(entity) { archetype, row ->
        return archetype.readRelationDataFor(row, kind, target, archetype.getRelations(kind, target))
    }

    override fun getRelationsFor(entity: EntityId, kind: ComponentId, target: EntityId): List<Relation> =
        records.runOn(entity) { archetype, _ -> archetype.getRelations(kind, target) }

    override fun has(entity: EntityId, componentId: ComponentId): Boolean =
        records.runOn(entity) { archetype, _ -> componentId in archetype }

    fun parentsOf(entity: EntityId): EntityIdArray {
        return getRelationsFor(entity, components.childOf, components.any)
            .map { it.target }
            .toSet()
            .toULongArray()
    }
}
