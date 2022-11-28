package com.mineinabyss.geary.engine

import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.systems.accessors.RelationWithData

public interface EntityReadOperations {
    /** Gets a [componentId]'s data from an [entity] or null if not present/the component doesn't hold any data. */
    public fun getComponentFor(entity: Entity, componentId: ComponentId): Component?

    /** Gets a list of all the components [entity] has, as well as relations in the form of [RelationComponent]. */
    public fun getComponentsFor(entity: Entity): Array<Component>

    /**
     * Gets relations in the same format as [Archetype.getRelations], but when kind/target [HOLDS_DATA], the appropriate
     * data is written to a [RelationWithData] object.
     */
    public fun getRelationsWithDataFor(
        entity: Entity,
        kind: ComponentId,
        target: EntityId
    ): List<RelationWithData<*, *>>

    public fun getRelationsFor(entity: Entity, kind: ComponentId, target: EntityId): List<Relation>


    /** Checks whether an [entity] has a [componentId] */
    public fun hasComponentFor(entity: Entity, componentId: ComponentId): Boolean

}
