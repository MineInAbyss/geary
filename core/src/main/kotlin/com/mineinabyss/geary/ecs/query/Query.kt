package com.mineinabyss.geary.ecs.query

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.geary
import com.mineinabyss.geary.ecs.api.entities.gearyNoMask
import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.api.systems.Family
import com.mineinabyss.geary.ecs.api.systems.FamilyBuilder
import com.mineinabyss.geary.ecs.engine.Archetype
import com.mineinabyss.geary.ecs.engine.ArchetypeIterator
import com.mineinabyss.geary.ecs.engine.HOLDS_DATA
import com.mineinabyss.geary.ecs.engine.QueryResult
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


public abstract class Query : Iterable<QueryResult> {
    internal val familyBuilder = FamilyBuilder()
    internal val dataKey = mutableListOf<GearyComponentId>()

    //idea is match works as a builder and family becomes immutable upon first access
    public val family: Family by lazy { familyBuilder.build() }
    internal val matchedArchetypes: MutableSet<Archetype> = mutableSetOf()


    public override fun iterator(): QueryIterator = QueryIterator(this)

    public fun registerAccessor(component: GearyComponentId) {
        familyBuilder.match += component
    }

    //TODO getOrNull
    protected inline fun <reified T : GearyComponent> get(): Accessor<T> = Accessor(componentId<T>() or HOLDS_DATA)

    public inline fun <reified T : GearyComponent> relation(): RelationAccessor<T> =
        RelationAccessor(Relation(parent = componentId<T>()), this)

    public inline fun <reified T : GearyComponent> relationWithData(): RelationAccessor<T> =
        RelationAccessor(Relation(parent = componentId<T>() or HOLDS_DATA), this)

    protected inline fun <reified T : GearyComponent> has(set: Boolean = true): GearyEntity {
        val componentId = componentId<T>().let { if (set) it and HOLDS_DATA.inv() else it }
        registerAccessor(componentId)
        return geary(componentId)
    }

    @Deprecated("Likely trying to access component off entity", ReplaceWith("entity.get()"))
    protected inline fun <reified T : GearyComponent> QueryResult.get(): Accessor<T> =
        error("Cannot change query at runtime")

}
