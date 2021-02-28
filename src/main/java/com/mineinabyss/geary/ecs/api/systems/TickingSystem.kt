package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import kotlin.reflect.KProperty

/**
 * A system for the ECS that will run every [interval] ticks.
 *
 * @param interval How often to run this system in ticks.
 *
 * @see [Engine.forEach]
 */
public abstract class TickingSystem(public val interval: Long = 1) {
    public abstract fun GearyEntity.tick()

    private val match = mutableListOf<Accessor<*>>()

    public fun tick() {
//TODO        Engine.getFamily(Family(match))
    }


    public operator fun <T : GearyComponent> Accessor<T>.provideDelegate(
        thisRef: Any?,
        property: KProperty<*>
    ): AccessorReader<T> {
        match += this
        return AccessorReader(this)
    }
}
