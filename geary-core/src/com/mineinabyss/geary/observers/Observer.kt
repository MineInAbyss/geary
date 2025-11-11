package com.mineinabyss.geary.observers

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.EntityId
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
    internal val onClose: (Observer) -> Unit,
) : AutoCloseable {
    var closed = false
        private set

    override fun close() {
        if (closed) return

        onClose(this)
        closed = true
    }
}

fun interface ObserverHandle {
    fun run(entity: EntityId, data: Any?, involvedComponent: ComponentId?)
}
