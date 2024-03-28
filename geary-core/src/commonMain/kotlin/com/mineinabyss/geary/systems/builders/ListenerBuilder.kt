package com.mineinabyss.geary.systems.builders

import com.mineinabyss.geary.components.RequestCheck
import com.mineinabyss.geary.components.events.FailedCheck
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.engine.Pipeline
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.systems.Listener
import com.mineinabyss.geary.systems.query.ListenerQuery

class ListenerBuilder<T : ListenerQuery>(
    val query: T,
    val pipeline: Pipeline,
) {
    fun exec(handle: T.() -> Unit): Listener<*> {
        query.initialize()
        val listener = Listener(
            query,
            query.buildFamilies(),
            handle
        )
        return pipeline.addListener(listener)
    }

    fun check(check: T.() -> Boolean): Listener<*> {
        query.initialize()
        val families = query.buildFamilies()
        val listener = Listener(
            query,
            families.copy(event = family {
                has<RequestCheck>()
                add(families.event)
            }),
        ) {
            if (!check()) event.entity.apply {
                remove<RequestCheck>()
                add<FailedCheck>()
            }
        }
        return pipeline.addListener(listener)
    }
}
