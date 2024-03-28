package com.mineinabyss.geary.systems

import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.systems.query.ListenerQuery

class Listener<T : ListenerQuery> internal constructor(
    val query: T,
    val families: ListenerQuery.Families,
    val handle: T.() -> Unit,
) {
    fun run() = handle(query)
    val event: Family.Selector.And get() = families.event
    val source: Family.Selector.And get() = families.source
    val target: Family.Selector.And get() = families.target
}
