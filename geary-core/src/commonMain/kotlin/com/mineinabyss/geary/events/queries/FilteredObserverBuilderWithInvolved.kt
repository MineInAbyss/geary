package com.mineinabyss.geary.events.queries

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.GearyEntityType
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.systems.query.Query

class FilteredObserverBuilder<R : Query, Context>(
    builder: ObserverBuilder<Query, Context>,
    matchQuery: R,
) {
    val inner = FilteredObserverBuilderWithInvolved(builder, matchQuery)
    fun exec(handle: Context.(R) -> Unit): Observer {
        return inner.exec { _, matching ->
            handle(matching)
        }
    }

}

class FilteredObserverBuilderWithInvolved<Q : Query, R : Query, Context>(
    val builder: ObserverBuilder<Q, Context>,
    val matchQuery: R,
) {
    fun exec(handle: Context.(Q, R) -> Unit): Observer {
        builder.query.initialize()
        matchQuery.initialize()
        val observer = object : Observer(
            listOf(builder.query, matchQuery),
            family { add(builder.query.buildFamily()); add(matchQuery.buildFamily()) },
            GearyEntityType(builder.involvedComponents),
            GearyEntityType(builder.events.listenToEvents),
            mustHoldData = builder.events.mustHoldData,
        ) {
            override fun run(entity: Entity, data: Any?, involvedComponent: ComponentId?) {
                builder.events.provideContext(entity, data).handle(builder.query, matchQuery)
            }
        }

        builder.events.module.eventRunner.addObserver(observer)
        return observer
    }
}
