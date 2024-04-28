package com.mineinabyss.geary.observers

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.EntityType
import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.systems.query.Query

abstract class Observer(
    val queries: List<Query>,
    val family: Family,
    val involvedComponents: EntityType,
    val listenToEvents: EntityType,
    val mustHoldData: Boolean,
) {
    abstract fun run(entity: Entity, data: Any?, involvedComponent: ComponentId?)
}
