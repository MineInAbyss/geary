package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.geary
import com.mineinabyss.geary.ecs.engine.Archetype
import com.mineinabyss.geary.ecs.engine.HOLDS_DATA
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
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
    internal val matchedArchetypes = mutableListOf<Archetype>()
    private var currComponents = listOf<GearyComponent>()

    private val traits = sortedSetOf<GearyComponentId>()
    private var accessorIndex = 0

    //idea is match works as a builder and family becomes immutable upon first access
    public val family: Family by lazy { Family(match.toList()) } //TODO make gearytype sortedSet

    public fun tick() {
        matchedArchetypes.forEach { arc ->
            traits.map {  }
            Archetype.ArchetypeIterator(arc, family.type).forEach { (entity, components) ->
                currComponents = components
                traits.forEach { trait ->

                    entity.tick()
                }
                entity.tick()
            }
        }
    }

    public abstract fun GearyEntity.tick()

    protected fun registerAccessor(component: GearyComponentId) {
        match.add(component)
    }

    public inline fun <reified T : GearyComponent> get(): Accessor<T> = Accessor(T::class)

    public inner class Accessor<T : GearyComponent>(kClass: KClass<T>) : ReadOnlyProperty<Any?, T> {
        private val index: Int = accessorIndex++

        init {
            val componentId = componentId(kClass) or HOLDS_DATA
            registerAccessor(componentId)
        }

        //TODO implement contracts for smart cast if Kotlin ever does so for lazy (this should essentially be identical)
        override fun getValue(thisRef: Any?, property: KProperty<*>): T {
            return currComponents[index] as T
        }
    }

    public inline fun <reified T : GearyComponent> trait(): TraitAccessor<T> = TraitAccessor(T::class)

    public class Trait<T : GearyComponent>(
        public val data: T,
        public val trait: GearyEntity,
        public val component: GearyEntity
    )

    public class TraitAccessor<T : GearyComponent>(kClass: KClass<T>) :
        ReadOnlyProperty<Any?, Trait<T>> {
        init {

        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): Trait<T> {
            TODO()
        }
    }

    protected inline fun <reified T : GearyComponent> has(): GearyEntity {
        val componentId = componentId<T>() and HOLDS_DATA.inv()
        registerAccessor(componentId)
        return geary(componentId)
    }
}
