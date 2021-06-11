package com.mineinabyss.geary.ecs.query

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.geary
import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.api.systems.Family
import com.mineinabyss.geary.ecs.engine.Archetype
import com.mineinabyss.geary.ecs.engine.ArchetypeIterator
import com.mineinabyss.geary.ecs.engine.HOLDS_DATA
import com.mineinabyss.geary.ecs.engine.QueryResult
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


public abstract class Query : Iterable<QueryResult> {
    private val match = sortedSetOf<GearyComponentId>()
    internal val dataKey = mutableListOf<GearyComponentId>()
    internal val relationsKey = mutableListOf<Relation>()

    //idea is match works as a builder and family becomes immutable upon first access
    public val family: Family by lazy { Family(match, relationsKey.toSortedSet()) }
    internal val matchedArchetypes: MutableSet<Archetype> = mutableSetOf()
    private val archetypeIterators = mutableMapOf<Archetype, ArchetypeIterator>()

    public inner class QueryIterator : Iterator<QueryResult> {
        private val archetypes = matchedArchetypes.toList().iterator()
        override fun hasNext(): Boolean {
            return archetypes.hasNext() || archetypeIterator.hasNext()
        }

        private var archetypeIterator = nextIterator()

        private fun nextIterator(): ArchetypeIterator {
            val arc = archetypes.next()
            return archetypeIterators[arc]?.copy()
                ?: ArchetypeIterator(arc, this@Query)
                    .also { archetypeIterators[arc] = it }
        }

        override fun next(): QueryResult {
            if (archetypeIterator.hasNext())
                archetypeIterator = nextIterator()
            return archetypeIterator.next()
        }
    }

    public override fun iterator(): QueryIterator = QueryIterator()

    protected fun registerAccessor(component: GearyComponentId) {
        match.add(component)
    }

    //TODO getOrNull
    protected inline fun <reified T : GearyComponent> get(): Accessor<T> = Accessor(componentId<T>() or HOLDS_DATA)

    public inner class Accessor<T : GearyComponent>(
        private val componentId: GearyComponentId
    ) : ReadOnlyProperty<QueryResult, T> {
        init {
            registerAccessor(componentId)
            dataKey.add(componentId)
        }

        private val index: Int = dataKey.indexOf(componentId)


        //TODO implement contracts for smart cast if Kotlin ever does so for lazy (this should essentially be identical)
        override fun getValue(thisRef: QueryResult, property: KProperty<*>): T {
            return thisRef.data[index] as T
        }
    }

    public inline fun <reified T : GearyComponent> relation(): RelationAccessor<T> =
        RelationAccessor(Relation(parent = componentId<T>()))

    public class RelationData<T : GearyComponent>(
        public val data: T,
        public val relation: GearyEntity,
        public val component: GearyEntity
    )

    public inner class RelationAccessor<T : GearyComponent>(
        private val relation: Relation
    ) : ReadOnlyProperty<QueryResult, RelationData<T>> {
        init {
            relationsKey.add(relation)
        }

        private val relationIndex: Int = relationsKey.indexOf(relation)

        override fun getValue(thisRef: QueryResult, property: KProperty<*>): RelationData<T> =
            RelationData(
                thisRef.relationCompData[relationIndex] as T,
                geary(relation.id),
                geary(thisRef.relationCompIds[relationIndex])
            )
    }

    protected inline fun <reified T : GearyComponent> has(set: Boolean = true): GearyEntity {
        val componentId = componentId<T>().let { if (set) it and HOLDS_DATA.inv() else it }
        registerAccessor(componentId)
        return geary(componentId)
    }
}
