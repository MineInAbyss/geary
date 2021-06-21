package com.mineinabyss.geary.ecs.query

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.geary
import com.mineinabyss.geary.ecs.api.entities.gearyNoMask
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
    private val andNot = sortedSetOf<GearyComponentId>()
    internal val relationsKey = mutableListOf<Relation>()

    //idea is match works as a builder and family becomes immutable upon first access
    public val family: Family by lazy { Family(match, relationsKey.toSortedSet(), andNot) }
    internal val matchedArchetypes: MutableSet<Archetype> = mutableSetOf()

    public inner class QueryIterator : Iterator<QueryResult> {
        private val archetypes = matchedArchetypes.toList().iterator()
        override fun hasNext(): Boolean {
            if (archetypeIterator?.hasNext() == true) return true
            if (!archetypes.hasNext()) return false

            while (archetypeIterator?.hasNext() == false) {
                if (!archetypes.hasNext()) return false
                archetypeIterator = nextIterator()
            }

            return true
        }

        private var archetypeIterator: ArchetypeIterator? = null

        init {
            if (hasNext()) archetypeIterator = nextIterator()
        }

        private fun nextIterator(): ArchetypeIterator {
            return archetypes.next().iteratorFor(this@Query)
        }

        override fun next(): QueryResult {
            return archetypeIterator!!.next()
        }
    }

    public override fun iterator(): QueryIterator = QueryIterator()

    protected fun registerAccessor(component: GearyComponentId) {
        match.add(component)
    }

    protected fun registerLackOf(component: GearyComponentId) {
        andNot.add(component)
    }

    //TODO getOrNull
    protected inline fun <reified T : GearyComponent> get(): Accessor<T> = Accessor(componentId<T>() or HOLDS_DATA)

    @Deprecated("Likely trying to access component off entity", ReplaceWith("entity.get()"))
    protected inline fun <reified T : GearyComponent> QueryResult.get(): Accessor<T> =
        error("Cannot change query at runtime")

    public inner class Accessor<T : GearyComponent>(
        private val componentId: GearyComponentId
    ) : ReadOnlyProperty<QueryResult, T> {
        init {
            registerAccessor(componentId)
            dataKey.add(componentId)
        }

        private val index: Int = dataKey.lastIndex


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

        private val relationIndex: Int = relationsKey.lastIndex

        override fun getValue(thisRef: QueryResult, property: KProperty<*>): RelationData<T> =
            RelationData(
                data = thisRef.relationCompData[relationIndex] as T,
                relation = gearyNoMask(relation.id),
                component = gearyNoMask(thisRef.relationCompIds[relationIndex])
            )
    }

    protected inline fun <reified T : GearyComponent> has(set: Boolean = false): GearyEntity {
        val componentId = componentId<T>().let { if (set) it or HOLDS_DATA else it }
        registerAccessor(componentId)
        return geary(componentId)
    }

    protected inline fun <reified T : GearyComponent> lacks(set: Boolean = false): GearyEntity {
        val componentId = componentId<T>().let { if (set) it or HOLDS_DATA else it }
        registerLackOf(componentId)
        return geary(componentId)
    }
}
