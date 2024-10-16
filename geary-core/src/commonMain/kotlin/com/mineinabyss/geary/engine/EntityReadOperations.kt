package com.mineinabyss.geary.engine

import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.systems.accessors.RelationWithData

interface EntityReadOperations {
    /** Gets a [componentId]'s data from an [entity] or null if not present/the component doesn't hold any data. */
    fun get(entity: EntityId, componentId: ComponentId): Component?

    /** Gets a list of all the components [entity] has, as well as relations in the form of [RelationComponent]. */
    fun getAll(entity: EntityId): Array<Component>

    /** Checks whether an [entity] is still active in the engine. */
    fun exists(entity: EntityId): Boolean

    /**
     * Gets relations in the same format as [Archetype.getRelations], but when kind/target [HOLDS_DATA], the appropriate
     * data is written to a [RelationWithData] object.
     */
    fun getRelationsWithDataFor(
        entity: EntityId,
        kind: ComponentId,
        target: EntityId
    ): List<RelationWithData<*, *>>

    fun getRelationsFor(entity: EntityId, kind: ComponentId, target: EntityId): List<Relation>


    /** Checks whether an [entity] has a [componentId] */
    fun has(entity: EntityId, componentId: ComponentId): Boolean

}
