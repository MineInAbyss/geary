package com.mineinabyss.geary.ecs.systems

import com.mineinabyss.geary.ecs.engine.Engine
import com.mineinabyss.geary.ecs.engine.forEach

/**
 * A system for the ECS that will run every [interval] ticks.
 *
 * @param interval How often to run this system in ticks.
 *
 * @see [Engine.forEach]
 */
public abstract class TickingSystem(public val interval: Long = 1) {
    public abstract fun tick()
}
