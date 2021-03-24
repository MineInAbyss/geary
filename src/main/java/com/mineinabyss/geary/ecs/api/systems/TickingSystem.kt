package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.geary
import com.mineinabyss.geary.ecs.engine.Archetype
import com.mineinabyss.geary.ecs.engine.HOLDS_DATA
import java.util.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * A system for the ECS that will run every [interval] ticks.
 *
 * @param interval How often to run this system in ticks.
 *
 * @see [Engine.forEach]
 */
public abstract class TickingSystem(public val interval: Long = 1) {
    private val match = sortedSetOf<GearyComponentId>()
    private val dataHolding = sortedSetOf<GearyComponentId>()
    private val traitIds = sortedSetOf<GearyComponentId>()

    internal val matchedArchetypes = mutableListOf<Archetype>()
    private var currComponents = listOf<GearyComponent>()

    /** Map of a trait's id without the last 32 bits defining components to its currently iterated component id. */
    private var traits = mapOf<GearyComponentId, GearyComponentId>()

    //idea is match works as a builder and family becomes immutable upon first access
    public val family: Family by lazy { Family(match, traitIds) } //TODO make gearytype sortedSet

    public fun tick() {
        // If any archetypes get added here while running through the system we dont want those entities to be iterated
        // right now, since they are most likely the entities involved with the current tick. To avoid this and
        // concurrent modifications, we make a copy of the list before iterating.
        matchedArchetypes.toList().forEach { arc ->
            val matchedTraits = arc.matchedTraits(family)
            val iterateTraits = matchedTraits.isNotEmpty()

            //TODO definitely could be done cleaner
            if (iterateTraits) {
                //TODO support multiple traits by iterating on all combinations of unique traits
                val (traitId, traitComponents) = matchedTraits.entries.first()
                Archetype.ArchetypeIterator(arc, family).forEach { (entity, components) ->
                    currComponents = components
                    traitComponents.forEach { compId ->
                        traits = mapOf(traitId to compId)
                        entity.tick()
                    }
                }
            } else Archetype.ArchetypeIterator(arc, family).forEach { (entity, components) ->
                currComponents = components
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
        private val index: Int by lazy { dataHolding.indexOf(componentId) }

        init {
            registerAccessor(componentId)
            dataHolding.add(componentId)
        }

        //TODO implement contracts for smart cast if Kotlin ever does so for lazy (this should essentially be identical)
        override fun getValue(thisRef: Any?, property: KProperty<*>): T {
            return currComponents[index] as T
        }
    }

    public inline fun <reified T : GearyComponent> trait(): TraitAccessor<T> =
        TraitAccessor(traitFor(componentId<T>()) or HOLDS_DATA)

    public class Trait<T : GearyComponent>(
        public val data: T,
        public val trait: GearyEntity,
        public val component: GearyEntity
    )

    public inner class TraitAccessor<T : GearyComponent>(
        private val traitId: GearyComponentId
    ) : ReadOnlyProperty<Any?, Trait<T>> {
        init {
            traitIds.add(traitId)
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): Trait<T> {
            val traitComponentId = traits[traitId] ?: TODO("Proper error")
            val fullId = traitFor(traitId, traitComponentId)
            //TODO dont get indexOf every time
            val index: Int by lazy { dataHolding.indexOf(fullId) }

            return Trait(currComponents[index] as T, geary(traitId), geary(fullId))
        }
    }

    protected inline fun <reified T : GearyComponent> has(): GearyEntity {
        val componentId = componentId<T>() and HOLDS_DATA.inv()
        registerAccessor(componentId)
        return geary(componentId)
    }
}
