package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.engine.Archetype
import com.mineinabyss.geary.ecs.engine.plusSorted
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
    private val match = mutableListOf<GearyComponentId>()
    public val family: Family = Family(match)
    internal val matchedArchetypes = mutableListOf<Archetype>()
    private var currComponents = listOf<GearyComponent>()

    private var accessorIndex = 0

    public fun tick() {
        matchedArchetypes.forEach { arc ->
            Archetype.ArchetypeIterator(arc, family.type).forEach { (entity, components) ->
                currComponents = components
                entity.tick()
            }
        }
    }

    public abstract fun GearyEntity.tick()

    protected fun registerAccessor(component: GearyComponentId) {
        match.plusSorted(component)
    }

    protected operator fun <T : GearyComponent> Accessor<T>.provideDelegate(
        thisRef: Any?,
        property: KProperty<*>
    ): AccessorReader<T> {
        val componentId =  componentId(kClass)

        registerAccessor(componentId)

        return AccessorReader(accessorIndex++)
    }

    protected inline fun <reified T: GearyComponent> has(): GearyComponentId {
        val componentId =  componentId<T>()
        registerAccessor(componentId)
        return componentId
    }

    public inner class AccessorReader<T : GearyComponent>(private val index: Int) : ReadOnlyProperty<Any?, T> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): T {
            return currComponents[index] as T
        }
    }

    public class Trait<T : GearyComponent>(kClass: KClass<T>) : Accessor<T>(kClass)

}
