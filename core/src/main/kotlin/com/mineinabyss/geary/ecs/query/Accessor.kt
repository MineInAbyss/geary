package com.mineinabyss.geary.ecs.query

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.engine.QueryResult
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

public class Accessor<T : GearyComponent>(
    componentId: GearyComponentId,
    query: Query
) : ReadOnlyProperty<QueryResult, T> {
    init {
        query.registerAccessor(componentId)
        query.dataKey.add(componentId)
    }

    private val index: Int = query.dataKey.lastIndex

    //TODO implement contracts for smart cast if Kotlin ever does so for lazy (this should essentially be identical)
    override fun getValue(thisRef: QueryResult, property: KProperty<*>): T {
        return thisRef.data[index] as T
    }
}
