package com.mineinabyss.geary.observers.builders

import com.mineinabyss.geary.datatypes.EntityType
import com.mineinabyss.geary.datatypes.GearyEntityType
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.engine.ComponentProvider
import com.mineinabyss.geary.observers.Observer
import com.mineinabyss.geary.systems.query.Query
import com.mineinabyss.geary.systems.query.ShorthandQuery


interface ExecutableObserver<Context> {
    fun filter(vararg queries: Query): ExecutableObserver<Context>

    fun exec(handle: context(Int) Context.() -> Unit): Observer

    fun <Q1 : Query> exec(query: Q1, handle: context(Int)  Context.(Q1) -> Unit): Observer {
        return filter(query).exec {
            handle(query)
        }
    }

    fun <Q1 : Query, Q2 : Query> exec(
        query1: Q1,
        query2: Q2,
        handle: context(Int)  Context.(Q1, Q2) -> Unit,
    ): Observer {
        return filter(query1).filter(query2).exec {
            handle(query1, query2)
        }
    }
}

data class QueryInvolvingObserverBuilder<Context, Q : ShorthandQuery>(
    val involvingQuery: Q,
    val inner: ObserverBuilder<Context>,
) {
    fun exec(handle: context(Int) Context.(Q) -> Unit): Observer {
        return inner.exec { handle(involvingQuery) }
    }

    fun <Q1 : Query> exec(query: Q1, handle: context(Int) Context.(Q, Q1) -> Unit): Observer {
        return inner.exec { handle(involvingQuery, query) }
    }
}

data class ObserverBuilder<Context>(
    val comp: ComponentProvider,
    val events: ObserverEventsBuilder<Context>,
    val involvedComponents: EntityType,
    val matchQueries: List<Query> = emptyList(),
) : ExecutableObserver<Context> {
    override fun filter(vararg queries: Query): ObserverBuilder<Context> {
        return copy(matchQueries = matchQueries + queries.toList())
    }

    override fun exec(handle: context(Int) Context.() -> Unit): Observer {
        val observer = Observer(
            matchQueries,
            family { matchQueries.forEach { add(it.family) } },
            involvedComponents,
            GearyEntityType(events.listenToEvents),
            events.mustHoldData,
            handle = { row, entity, data, _ ->
                with(row) { events.provideContext(entity, data).handle() }
            },
            onClose = { events.world.eventRunner.removeObserver(it) }
        )
        events.onBuild(observer)
        return observer
    }
}
