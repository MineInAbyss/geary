package com.mineinabyss.geary.engine.archetypes.operations

import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.datatypes.maps.ArrayTypeMap
import com.mineinabyss.geary.engine.Components
import com.mineinabyss.geary.engine.EntityMutateOperations
import com.mineinabyss.geary.engine.QueryManager
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.engine.archetypes.ArchetypeProvider
import com.mineinabyss.geary.helpers.NO_COMPONENT
import com.mineinabyss.geary.helpers.async.AsyncCatcher
import com.mineinabyss.geary.helpers.async.catch
import com.mineinabyss.geary.observers.EventRunner
import com.mineinabyss.geary.observers.events.OnExtend

class ArchetypeMutateOperations(
    private val archetypeProvider: ArchetypeProvider,
    private val records: ArrayTypeMap,
    private val components: Components,
    private val eventRunner: EventRunner,
    private val queryManager: QueryManager,
    private val asyncCatcher: AsyncCatcher,
) : EntityMutateOperations {
    override fun setComponentFor(
        entity: EntityId,
        componentId: ComponentId,
        data: Component,
        noEvent: Boolean,
    ) {
        asyncCatcher.catch { "Async setComponent for $entity, component $componentId" }
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
        asyncCatcher.catch { "Async addComponent for $entity, component $componentId" }
        records.runOn(entity) { archetype, row ->
            addComponent(archetype, row, componentId.withoutRole(HOLDS_DATA), !noEvent)
        }
    }

    override fun extendFor(entity: EntityId, base: EntityId) {
        asyncCatcher.catch { "Async extend for $entity, base $base" }
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

        // Mark instance a InstanceOf baseEntity
        addComponent(instanceArch, instanceRow, Relation.of(components.instanceOf, baseEntity).id, true) { arch, row ->
            instanceArch = arch; instanceRow = row
        }

        val basePrefabs = baseArchetype.getRelationsByKind(components.instanceOf)

        // Don't inherit components marked as NoInherit, nor baseEntity's prefabs
        val noInheritComponents: List<EntityId> = baseArchetype
            .getRelationsByKind(components.noInherit)
            .map { Relation.of(it).target }
            .plus(basePrefabs.map { it })

        // Add all components without data
        baseArchetype.type.filter { !it.holdsData() && it !in noInheritComponents }.forEach {
            addComponent(instanceArch, instanceRow, it, true) { arch, row -> instanceArch = arch; instanceRow = row }
        }

        // Add all components with data
        baseArchetype.dataHoldingType.forEach {
            if (it.withoutRole(HOLDS_DATA) in noInheritComponents) return@forEach
            setComponent(instanceArch, instanceRow, it, baseArchetype.getUnsafe(baseRow, it), true) { arch, row ->
                instanceArch = arch; instanceRow = row
            }
        }

        // Children of instantiated prefabs should be children of the instance
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
        asyncCatcher.catch { "Async remove for $entity, component $componentId" }
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
        asyncCatcher.catch { "Async clear for $entity" }
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
