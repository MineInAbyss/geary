package com.mineinabyss.geary.ecs.query

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.gearyNoMask
import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.engine.QueryResult
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

public open class RelationAccessor<T : GearyComponent>(
    private val relation: Relation,
    private val query: Query,
) : ReadOnlyProperty<QueryResult, RelationData<T>> {
    init {
        query.familyBuilder.relations.add(relation)
    }

    private val relationIndex: Int = query.familyBuilder.relations.lastIndex

    override fun getValue(thisRef: QueryResult, property: KProperty<*>): RelationData<T> =
        RelationData(
            data = thisRef.relationData[relationIndex] as T,
            relation = gearyNoMask(relation.id),
            component = gearyNoMask(thisRef.relationComponentIds[relationIndex])
        )
}

public class RelationAccessorWithData<T : GearyComponent>(
    relation: Relation,
    query: Query
) : RelationAccessor<T>(relation, query) {
    init {
        query.familyBuilder.relations.add(relation)
    }

    override fun getValue(thisRef: QueryResult, property: KProperty<*>): RelationData<T> =
        RelationData(
            data = thisRef.relationData[relationIndex] as T,
            relation = gearyNoMask(relation.id),
            component = gearyNoMask(thisRef.relationComponentIds[relationIndex])
        )
}

public class RelationData<T : GearyComponent>(
    public val data: T,
    public val relation: GearyEntity,
    public val component: GearyEntity
)
