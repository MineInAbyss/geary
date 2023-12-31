package com.mineinabyss.geary.datatypes

/** When a component extending this interface is registered, geary will call [onCreate] to modify the component entity. */
interface ComponentDefinition {
    fun onCreate(component: Entity) {}
}
