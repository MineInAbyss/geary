package com.mineinabyss.geary.ecs.accessors

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.systems.GearyListener
import com.mineinabyss.geary.ecs.api.systems.GearySystem
import com.mineinabyss.geary.ecs.engine.Archetype

/**
 * A scope provided to [Accessor]s to cache data per archetype.
 */
public open class ArchetypeCacheScope(
    public val archetype: Archetype,
    public val perArchetypeData: List<List<Any?>>,
)

/**
 * An [ArchetypeCacheScope] with a reference to a specific entity in that archetype.
 * It will be processed by an [Accessor] into a [ResultScope].
 *
 * Note: This extends [ArchetypeCacheScope] to allow accessors to read their per archetype cache when processing data.
 *
 * @see Accessor
 */
public class RawAccessorDataScope(
    archetype: Archetype,
    perArchetypeData: List<List<Any?>>,
    public val row: Int,
) : ArchetypeCacheScope(archetype, perArchetypeData) {
    public val entity: GearyEntity = archetype.getEntity(row)
}

/**
 * A generic scope for processed [RawAccessorDataScope] data.
 *
 * Other scopes extend this scope without changing much so that we can get type safe access when
 * building accessors. For instance, a [GearyListener] can have accessors for a source, target,
 * or event entity which each contain different data.
 *
 * @see SourceScope
 * @see TargetScope
 * @see EventScope
 */
public open class ResultScope(
    public val entity: GearyEntity,
    internal val data: List<*>,
)

/**
 * Stores processed data for the source entity in an event.
 *
 * This is the entity that caused the event.
 *
 * @see TargetScope
 * @see EventScope
 */
public open class SourceScope(
    entity: GearyEntity,
    data: List<*>,
) : ResultScope(entity, data)

/**
 * Stores processed data for the target entity in any [GearySystem].
 *
 * This is the entity being affected by in event or system.
 *
 * @see SourceScope
 * @see EventScope
 */
public open class TargetScope(
    entity: GearyEntity,
    data: List<*>,
) : ResultScope(entity, data)

/**
 * Stores processed data for the event entity in an event.
 *
 * This entity stores instructions in the form of components that will typically affect the source or target.
 *
 * @see SourceScope
 * @see TargetScope
 */
public class EventScope(
    entity: GearyEntity,
    data: List<*>,
) : ResultScope(entity, data)
