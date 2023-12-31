package com.mineinabyss.geary.engine.archetypes.operations

import com.mineinabyss.geary.components.relations.InstanceOf
import com.mineinabyss.geary.components.relations.NoInherit
import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.datatypes.maps.TypeMap
import com.mineinabyss.geary.engine.EntityMutateOperations
import com.mineinabyss.geary.engine.archetypes.ArchetypeProvider
import com.mineinabyss.geary.modules.archetypes

class ArchetypeMutateOperations : EntityMutateOperations {
    private val records: TypeMap get() = archetypes.records
    private val archetypeProvider: ArchetypeProvider get() = archetypes.archetypeProvider

    override fun setComponentFor(
        entity: Entity,
        componentId: ComponentId,
        data: Component,
        noEvent: Boolean
    ) {
        records[entity].apply {
            // Only add HOLDS_DATA if this isn't a relation. All relations implicitly hold data currently and that bit
            // corresponds to the component part of the relation.
            val componentWithRole = componentId.withRole(HOLDS_DATA)
            archetype.setComponent(this, componentWithRole, data, !noEvent)
        }
    }

    override fun addComponentFor(
        entity: Entity,
        componentId: ComponentId,
        noEvent: Boolean
    ) {
        records[entity].apply {
            archetype.addComponent(this, componentId.withoutRole(HOLDS_DATA), !noEvent)
        }
    }

    override fun addPrefabFor(entity: Entity, prefab: Entity) {
        records[entity].apply {
            archetype.addExtends(this, records[prefab])
            addRelation<InstanceOf>(prefab)
            //TODO this isn't copying over any relations
            val comp = prefab.getAll().toMutableSet()
            prefab.getRelationsWithData<NoInherit?, Any>().forEach {
                comp -= it.targetData
            }
            prefab.children.forEach { it.addParent(this) }
            setAll(comp, override = false) //TODO plan out more thoroughly and document overriding behaviour
            prefab.with { copy: CopyToInstances ->
                copy.decodeComponentsTo(this, override = false)
            }
        }
    }

    override fun removeComponentFor(entity: Entity, componentId: ComponentId): Boolean =
        records[entity].run {
            val a = archetype.removeComponent(this, componentId.withRole(HOLDS_DATA))
            val b = archetype.removeComponent(this, componentId.withoutRole(HOLDS_DATA))
            a || b // return whether anything was changed
        }

    override fun clearEntity(entity: Entity) {
        val record = records[entity]
        record.archetype.removeEntity(record.row)
        archetypeProvider.rootArchetype.createWithoutData(entity, record)
    }
}
