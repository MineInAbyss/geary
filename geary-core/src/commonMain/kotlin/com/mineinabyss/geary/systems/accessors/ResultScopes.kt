package com.mineinabyss.geary.systems.accessors

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.GearySystem

/**
 * A generic scope for processed data.
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
    //TODO this should be hidden
    public val data: List<*>,
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
