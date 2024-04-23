package com.mineinabyss.geary.events.queries

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.GearyEntityType
import com.mineinabyss.geary.systems.accessors.type.ComponentAccessor
import com.mineinabyss.geary.systems.query.Query


fun interface ExecutableObserver<Handle> {
    fun exec(handle: Handle): Observer
}

data class ObserverBuilder<Q : Query, Context>(
    val events: ObserverEventsBuilder<Context>,
    val query: Q,
) : ExecutableObserver<Context.(Q) -> Unit> {
    val involvedComponents = query.accessors.filterIsInstance<ComponentAccessor<*>>().map { it.id }

    fun <R : Query> filter(otherQuery: R) = FilteredObserverBuilderWithInvolved(this, otherQuery)

    override fun exec(handle: Context.(Q) -> Unit): Observer {
        query.initialize()
        val observer = object : Observer(
            listOf(query),
            query.buildFamily(),
            GearyEntityType(involvedComponents),
            GearyEntityType(events.listenToEvents),
            events.mustHoldData,
        ) {
            override fun run(entity: Entity, data: Any?, involvedComponent: ComponentId?) {
                events.provideContext(entity, data).handle(query)
            }
        }

        events.module.eventRunner.addObserver(observer)
        return observer
    }
}
