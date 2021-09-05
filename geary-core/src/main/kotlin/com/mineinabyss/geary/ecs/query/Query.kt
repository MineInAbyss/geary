package com.mineinabyss.geary.ecs.query

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.relations.RelationParent
import com.mineinabyss.geary.ecs.api.systems.MutableAndSelector
import com.mineinabyss.geary.ecs.api.systems.QueryManager
import com.mineinabyss.geary.ecs.engine.Archetype
import com.mineinabyss.geary.ecs.engine.HOLDS_DATA
import com.mineinabyss.geary.ecs.engine.iteration.QueryResult
import com.mineinabyss.geary.ecs.query.accessors.*

/**
 * @property family The Query itself is a mutable family builder, [family] is the built, immutable version of it.
 * @property matchedArchetypes A set of archetypes which have been matched to this query.
 */
public abstract class Query(
    init: (Query.() -> Unit)? = null
) : Iterable<QueryResult>, MutableAndSelector() {
    public val family: AndSelector by lazy { build() }
    internal val matchedArchetypes: MutableSet<Archetype> = mutableSetOf()
    internal val accessors = mutableListOf<Accessor<*>>()

    private var registered = false

    public override fun iterator(): QueryIterator {
        if (!registered) {
            QueryManager.trackQuery(this)
            registered = true
        }
        return QueryIterator(this)
    }

    //TODO getOrNull
    protected inline fun <reified T : GearyComponent> get(): ComponentAccessor<T> {
        val component = componentId<T>() or HOLDS_DATA
        has(component)
        return ComponentAccessor(this, component)
    }

    public inline fun <reified T : GearyComponent> relation(): RelationAccessor<T> {
        val relationParent = RelationParent(componentId<T>())
        has(relationParent)
        return RelationAccessor(this, relationParent)
    }

    public inline fun <reified T : GearyComponent> relationWithData(): RelationWithDataAccessor<T> {
        val relationParent = RelationParent(componentId<T>())
        has(relationParent, componentMustHoldData = true)
        return RelationWithDataAccessor(this, relationParent)
    }

    public inline fun <reified T : GearyComponent> allRelationsWithData(): RelationListAccessor<T> {
        val relationParent = RelationParent(componentId<T>())
        has(relationParent, componentMustHoldData = true)
        return RelationListAccessor(this, relationParent)
    }


    @Deprecated("Likely trying to access component off entity", ReplaceWith("entity.get()"))
    protected inline fun <reified T : GearyComponent> QueryResult.get(): ComponentAccessor<T> =
        error("Cannot change query at runtime")

    init {
        if (init != null) init()
    }
}
