package com.mineinabyss.geary.ecs.systems

abstract class TickingSystem(val interval: Int = 1) {
    abstract fun tick()
}