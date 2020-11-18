package com.mineinabyss.geary.ecs.systems

public abstract class TickingSystem(public val interval: Int = 1) {
    public abstract fun tick()
}
