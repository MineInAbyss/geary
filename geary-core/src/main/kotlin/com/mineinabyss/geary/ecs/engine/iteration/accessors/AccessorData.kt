package com.mineinabyss.geary.ecs.engine.iteration.accessors

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.engine.Archetype
import com.mineinabyss.geary.ecs.query.accessors.Accessor

/**
 * Some raw data [Accessor]s process to eventually build up to a [QueryResult].
 *
 * @see Accessor
 */
public class AccessorDataScope(
    public val archetype: Archetype,
    public val row: Int,
    public val entity: GearyEntity,
)


//TODO better name now that this isn't query specific
/**
 * Stores data which is formatted
 */
public data class QueryResult(
    val entity: GearyEntity,
    internal val data: List<*>
)
