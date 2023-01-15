package com.mineinabyss.geary.systems.accessors

import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.systems.GearySystem
import com.mineinabyss.geary.systems.Listener

/**
 * A generic scope for processed data.
 *
 * Other scopes extend this scope without changing much so that we can get type safe access when
 * building accessors. For instance, a [Listener] can have accessors for a source, target,
 * or event entity which each contain different data.
 *
 * @see SourceScope
 * @see TargetScope
 * @see EventScope
 */
open class ResultScope(
    val entity: Entity,
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
open class SourceScope(
    entity: Entity,
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
open class TargetScope(
    entity: Entity,
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
class EventScope(
    entity: Entity,
    data: List<*>,
) : ResultScope(entity, data)
