package com.mineinabyss.geary.ecs.query

import com.mineinabyss.geary.ecs.accessors.Accessor
import com.mineinabyss.geary.ecs.accessors.AccessorHolder
import com.mineinabyss.geary.ecs.accessors.ComponentAccessor
import com.mineinabyss.geary.ecs.accessors.TargetScope
import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.systems.QueryManager
import com.mineinabyss.geary.ecs.engine.Archetype
import kotlin.reflect.KProperty

/**com.mineinabyss.geary.ecs.engine.iteration.accessors
 * @property matchedArchetypes A set of archetypes which have been matched to this query.
 */
public abstract class Query : Iterable<TargetScope>, AccessorHolder() {
    internal val matchedArchetypes: MutableSet<Archetype> = mutableSetOf()

    private var registered = false

    //TODO restrict to calls within the scope of a Query
    public override fun iterator(): QueryIterator {
        if (!registered) {
            QueryManager.trackQuery(this)
            registered = true
        }
        return QueryIterator(this)
    }

    @Suppress("unused") // Specifically
    @Deprecated("Likely trying to access component off entity", ReplaceWith("entity.get()"))
    protected inline fun <reified T : GearyComponent> TargetScope.get(): ComponentAccessor<T> =
        error("Cannot change query at runtime")

    @Suppress("UNCHECKED_CAST")
    public operator fun <T> Accessor<T>.getValue(thisRef: TargetScope, property: KProperty<*>): T =
        thisRef.data[index] as T

}

public operator fun <T : Query, R> T.invoke(run: T.() -> R): R = run(run)
