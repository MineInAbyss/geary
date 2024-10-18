package com.mineinabyss.geary.engine.archetypes.operations

import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.datatypes.maps.ArrayTypeMap
import com.mineinabyss.geary.engine.Components
import com.mineinabyss.geary.engine.EntityMutateOperations
import com.mineinabyss.geary.engine.QueryManager
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.engine.archetypes.ArchetypeProvider
import com.mineinabyss.geary.helpers.NO_COMPONENT
import com.mineinabyss.geary.observers.EventRunner
import com.mineinabyss.geary.observers.events.OnExtend

class ArchetypeMutateOperations(
    private val archetypeProvider: ArchetypeProvider,
    private val records: ArrayTypeMap,
    private val components: Components,
    private val eventRunner: EventRunner,
    private val queryManager: QueryManager,
) : EntityMutateOperations {
    override fun setComponentFor(
        entity: EntityId,
        componentId: ComponentId,
        data: Component,
        noEvent: Boolean,
    ) {
        records.runOn(entity) { archetype, row ->
            // Only add HOLDS_DATA if this isn't a relation. All relations implicitly hold data currently and that bit
            // corresponds to the component part of the relation.
            val componentWithRole = componentId.withRole(HOLDS_DATA)
            setComponent(archetype, row, componentWithRole, data, !noEvent)
        }
    }

    override fun addComponentFor(
        entity: EntityId,
        componentId: ComponentId,
        noEvent: Boolean,
    ) {
        records.runOn(entity) { archetype, row ->
            addComponent(archetype, row, componentId.withoutRole(HOLDS_DATA), !noEvent)
        }
    }

    override fun extendFor(entity: EntityId, base: EntityId) {
        records.runOn(base) { archetype, row ->
            records.runOn(entity) { entityArch, entityRow ->
                instantiateTo(archetype, row, entityArch, entityRow)
            }
        }
    }

    @Suppress("NAME_SHADOWING") // Want to make sure original arch/row is not accidentally accessed
    private fun instantiateTo(
        baseArchetype: Archetype,
        baseRow: Int,
        instanceArch: Archetype,
        instanceRow: Int,
        callEvent: Boolean = true,
    ) {
        val baseEntity = baseArchetype.getEntity(baseRow)
        val instanceEntity = instanceArch.getEntity(instanceRow)
        var instanceArch = instanceArch
        var instanceRow = instanceRow

        addComponent(instanceArch, instanceRow, Relation.of(components.instanceOf, baseEntity).id, true) { arch, row ->
            instanceArch = arch; instanceRow = row
        }

        val noInheritComponents = baseArchetype.getRelationsByKind(components.noInherit).map { Relation.of(it).target }
        baseArchetype.type.filter { !it.holdsData() && it !in noInheritComponents }.forEach {
            addComponent(instanceArch, instanceRow, it, true) { arch, row -> instanceArch = arch; instanceRow = row }
        }
        baseArchetype.dataHoldingType.forEach {
            if (it.withoutRole(HOLDS_DATA) in noInheritComponents) return@forEach
            setComponent(instanceArch, instanceRow, it, baseArchetype.getUnsafe(baseRow, it), true) { arch, row ->
                instanceArch = arch; instanceRow = row
            }
        }

        queryManager.childrenOf(baseEntity).forEach { child ->
            // Add instanceEntity as parent
            addComponentFor(instanceEntity, components.couldHaveChildren, true)
            addComponentFor(child, Relation.of(components.childOf, instanceEntity).id, false)
        }
        records.runOn(instanceEntity) { arch, row -> instanceArch = arch; instanceRow = row }

        if (callEvent) eventRunner.callEvent(
            components.onExtend,
            OnExtend(baseEntity),
            NO_COMPONENT,
            instanceArch.getEntity(instanceRow)
        )
    }

    override fun removeComponentFor(entity: EntityId, componentId: ComponentId, noEvent: Boolean): Boolean {
        val a = records.runOn(entity) { archetype, row ->
            archetype.removeComponent(row, componentId.withRole(HOLDS_DATA), onModify = { moveTo, newRow, onComplete ->
                if (!noEvent) callComponentModifyEvent(moveTo, components.onRemove, componentId, newRow, onComplete)
            })
        }
        val b = records.runOn(entity) { archetype, row ->
            archetype.removeComponent(
                row,
                componentId.withoutRole(HOLDS_DATA),
                onModify = { moveTo, newRow, onComplete ->
                    if (!noEvent) callComponentModifyEvent(moveTo, components.onRemove, componentId, newRow, onComplete)
                })
        }
        return a || b // return whether anything was changed
    }

    @Deprecated("Use removeComponentFor(entity, componentId, noEvent) instead.")
    override fun removeComponentFor(entity: EntityId, componentId: ComponentId): Boolean =
        removeComponentFor(entity, componentId, false)

    override fun clearEntity(entity: EntityId) {
        records.runOn(entity) { archetype, row ->
            archetype.removeEntity(row)
            val newRow = archetypeProvider.rootArchetype.createWithoutData(entity)
            records[entity, archetypeProvider.rootArchetype] = newRow
        }
    }

    private inline fun callComponentModifyEvent(
        archetype: Archetype,
        eventType: ComponentId,
        involvedComp: ComponentId,
        row: Int,
        onComplete: (Archetype, row: Int) -> Unit = { _, _ -> },
    ) {
        val entity = archetype.getEntity(row)
        callComponentModifyEvent(archetype, eventType, involvedComp, row)
        // Don't have any way to know final archetype and row without re-reading
        records.runOn(entity, onComplete)
    }

    private fun callComponentModifyEvent(
        archetype: Archetype,
        eventType: ComponentId,
        involvedComp: ComponentId,
        row: Int,
    ) {
        val entity = archetype.getEntity(row)
        eventRunner.callEvent(eventType, null, involvedComp, entity)
    }

    private inline fun addComponent(
        archetype: Archetype,
        row: Int,
        componentId: ComponentId,
        callEvent: Boolean,
        onUpdated: (Archetype, row: Int) -> Unit = { _, _ -> },
    ) {
        archetype.addComponent(row, componentId, onUpdated = { moveTo, newRow ->
            if (callEvent) callComponentModifyEvent(moveTo, components.onAdd, componentId, newRow, onUpdated)
        })
    }

    @Suppress("NAME_SHADOWING") // Want to make sure original arch/row is not accidentally accessed
    private inline fun setComponent(
        archetype: Archetype,
        row: Int,
        componentId: ComponentId,
        data: Component,
        callEvent: Boolean,
        onUpdated: (Archetype, row: Int) -> Unit = { _, _ -> },
    ) {
        archetype.setComponent(row, componentId, data, onUpdated = { firstSet, archetype, row ->
            if (callEvent) callComponentModifyEvent(archetype, components.onSet, componentId, row) { archetype, row ->
                if (firstSet) callComponentModifyEvent(archetype, components.onFirstSet, componentId, row, onUpdated)
                else onUpdated(archetype, row)
            } else onUpdated(archetype, row)
        })
    }
}
