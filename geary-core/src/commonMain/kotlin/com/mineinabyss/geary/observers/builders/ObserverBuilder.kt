package com.mineinabyss.geary.observers.builders

import com.mineinabyss.geary.datatypes.EntityType
import com.mineinabyss.geary.datatypes.GearyEntityType
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.observers.Observer
import com.mineinabyss.geary.systems.query.Query
import com.mineinabyss.geary.systems.query.ShorthandQuery


interface ExecutableObserver<Context> {
    fun filter(vararg queries: Query): ExecutableObserver<Context>

    fun exec(handle: Context.() -> Unit): Observer

    fun <Q1 : Query> exec(query: Q1, handle: Context.(Q1) -> Unit): Observer {
        return filter(query).exec {
            handle(query)
        }
    }

    fun <Q1 : Query, Q2: Query> exec(query1: Q1, query2: Q2,handle: Context.(Q1, Q2) -> Unit): Observer {
        return filter(query1).filter(query2).exec {
            handle(query1, query2)
        }
    }
}

data class QueryInvolvingObserverBuilder<Context, Q : ShorthandQuery>(
    val involvingQuery: Q,
    val inner: ObserverBuilder<Context>
) {
    fun exec(handle: Context.(Q) -> Unit): Observer {
        return inner.exec { handle(involvingQuery) }
    }

    fun <Q1 : Query> exec(query: Q1, handle: Context.(Q, Q1) -> Unit): Observer {
        return inner.exec { handle(involvingQuery, query) }
    }
}

data class ObserverBuilder<Context>(
    val events: ObserverEventsBuilder<Context>,
    val involvedComponents: EntityType,
    val matchQueries: List<Query> = emptyList(),
) : ExecutableObserver<Context> {

    override fun filter(vararg queries: Query): ObserverBuilder<Context> {
        return copy(matchQueries = matchQueries + queries.toList())
    }


    override fun exec(handle: Context.() -> Unit): Observer {
        val observer = Observer(
            matchQueries,
            family { matchQueries.forEach { add(it.buildFamily()) } },
            involvedComponents,
            GearyEntityType(events.listenToEvents),
            events.mustHoldData,
            handle = { entity, data, _ ->
                events.provideContext(entity, data).handle()
            }
        )
        events.onBuild(observer)
        return observer
    }
}
