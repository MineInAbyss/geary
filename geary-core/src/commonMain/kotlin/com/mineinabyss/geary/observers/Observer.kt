package com.mineinabyss.geary.observers

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.EntityType
import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.systems.query.Query

data class Observer(
    val queries: List<Query>,
    val family: Family,
    val involvedComponents: EntityType,
    val listenToEvents: EntityType,
    val mustHoldData: Boolean,
    val handle: ObserverHandle,
)

fun interface ObserverHandle {
    fun run(entity: Entity, data: Any?, involvedComponent: ComponentId?)
}
