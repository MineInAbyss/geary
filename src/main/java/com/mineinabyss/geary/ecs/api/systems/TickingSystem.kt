package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.geary
import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.engine.Archetype
import com.mineinabyss.geary.ecs.engine.ArchetypeIterator
import com.mineinabyss.geary.ecs.engine.HOLDS_DATA
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * A system for the ECS that will run every [interval] ticks.
 *
 * @param interval How often to run this system in ticks.
 *
 * @see [ArchetypeIterator]
 */
public abstract class TickingSystem(public val interval: Long = 1) {
    private val match = sortedSetOf<GearyComponentId>()
    private val dataKey = sortedSetOf<GearyComponentId>()
    private val relationsKey = sortedSetOf<Relation>()

    internal val matchedArchetypes = mutableListOf<Archetype>()
    private var currComponents = listOf<GearyComponent>()
    private var currRelationIds = listOf<GearyComponentId>()

    /** The data of the current component associated with each relation */
    private var currRelationData = listOf<GearyComponent>()

    //idea is match works as a builder and family becomes immutable upon first access
    public val family: Family by lazy { Family(match, relationsKey) } //TODO make gearytype sortedSet

    private val archetypeIterators = mutableMapOf<Archetype, ArchetypeIterator>()

    public fun tick() {
        // If any archetypes get added here while running through the system we dont want those entities to be iterated
        // right now, since they are most likely the entities involved with the current tick. To avoid this and
        // concurrent modifications, we make a copy of the list before iterating.
        matchedArchetypes.toList().forEach { arc ->
            val iterator = archetypeIterators.getOrPut(arc, { ArchetypeIterator(arc, family) })
            iterator.reset()
            iterator.forEach { (entity, components, relationIds, relationData) ->
                currComponents = components
                currRelationIds = relationIds
                currRelationData = relationData

                entity.tick()
            }
        }
    }

    public abstract fun GearyEntity.tick()

    protected fun registerAccessor(component: GearyComponentId) {
        match.add(component)
    }

    public inline fun <reified T : GearyComponent> get(): Accessor<T> = Accessor(componentId<T>() or HOLDS_DATA)

    public inner class Accessor<T : GearyComponent>(
        private val componentId: GearyComponentId
    ) : ReadOnlyProperty<Any?, T> {
        init {
            registerAccessor(componentId)
            dataKey.add(componentId)
        }

        private val index: Int = dataKey.indexOf(componentId)


        //TODO implement contracts for smart cast if Kotlin ever does so for lazy (this should essentially be identical)
        override fun getValue(thisRef: Any?, property: KProperty<*>): T {
            return currComponents[index] as T
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
    ) : ReadOnlyProperty<Any?, RelationData<T>> {
        init {
            relationsKey.add(relation)
        }

        private val relationIndex: Int = relationsKey.indexOf(relation)

        override fun getValue(thisRef: Any?, property: KProperty<*>): RelationData<T> =
            RelationData(
                currRelationData[relationIndex] as T,
                geary(relation.id),
                geary(currRelationIds[relationIndex])
            )
    }

    protected inline fun <reified T : GearyComponent> has(): GearyEntity {
        val componentId = componentId<T>() and HOLDS_DATA.inv()
        registerAccessor(componentId)
        return geary(componentId)
    }
}
