package com.mineinabyss.geary.ecs.query

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.api.relations.RelationParent
import com.mineinabyss.geary.ecs.api.systems.MutableAndSelector
import com.mineinabyss.geary.ecs.engine.Archetype
import com.mineinabyss.geary.ecs.engine.HOLDS_DATA
import com.mineinabyss.geary.ecs.engine.QueryResult

public abstract class Query : Iterable<QueryResult>, MutableAndSelector() {
    internal val dataKey = mutableListOf<GearyComponentId>()

    //idea is match works as a builder and family becomes immutable upon first access
    public val family: AndSelector by lazy { build() }
    internal val matchedArchetypes: MutableSet<Archetype> = mutableSetOf()

    public override fun iterator(): QueryIterator = QueryIterator(this)

    //TODO getOrNull
    protected inline fun <reified T : GearyComponent> get(): Accessor<T> {
        val component = componentId<T>() or HOLDS_DATA
        has(component)
        return Accessor(component, this)
    }

    public inline fun <reified T : GearyComponent> relation(): RelationAccessor<T> {
        val relation = RelationParent(componentId<T>())
        has(relation)
        return RelationAccessor(relation, this)
    }

//    public inline fun <reified T : GearyComponent> relationWithData(): RelationAccessor<T> =
//        RelationAccessor(Relation(parent = componentId<T>() or HOLDS_DATA), this)


    @Deprecated("Likely trying to access component off entity", ReplaceWith("entity.get()"))
    protected inline fun <reified T : GearyComponent> QueryResult.get(): Accessor<T> =
        error("Cannot change query at runtime")

}
