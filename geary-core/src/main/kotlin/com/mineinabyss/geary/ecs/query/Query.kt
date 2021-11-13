package com.mineinabyss.geary.ecs.query

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.systems.QueryManager
import com.mineinabyss.geary.ecs.engine.Archetype
import com.mineinabyss.geary.ecs.engine.iteration.accessors.AccessorHolder
import com.mineinabyss.geary.ecs.engine.iteration.accessors.QueryResult
import com.mineinabyss.geary.ecs.query.accessors.ComponentAccessor

/**com.mineinabyss.geary.ecs.engine.iteration.accessors
 * @property matchedArchetypes A set of archetypes which have been matched to this query.
 */
public abstract class Query : Iterable<QueryResult>, AccessorHolder() {
    internal val matchedArchetypes: MutableSet<Archetype> = mutableSetOf()

    private var registered = false

    public override fun iterator(): QueryIterator {
        if (!registered) {
            QueryManager.trackQuery(this)
            registered = true
        }
        return QueryIterator(this)
    }

    @Deprecated("Likely trying to access component off entity", ReplaceWith("entity.get()"))
    protected inline fun <reified T : GearyComponent> QueryResult.get(): ComponentAccessor<T> =
        error("Cannot change query at runtime")
}
