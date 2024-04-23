package com.mineinabyss.geary.events.queries

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.EntityType
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.datatypes.entityTypeOf
import com.mineinabyss.geary.helpers.cId
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.systems.query.Query
import com.mineinabyss.geary.systems.query.QueryShorthands
import com.mineinabyss.geary.systems.query.ShorthandQuery

data class ObserverWithoutData(
    override val listenToEvents: List<ComponentId>,
    override val module: GearyModule,
) : ObserverEventsBuilder<ObserverContext>() {
    override val mustHoldData: Boolean = false
    inline fun <reified R> or() = copy(listenToEvents = listenToEvents + componentId<R>())

    override fun provideContext(entity: GearyEntity, data: Any?) = ObserverContext(entity)
}

data class ObserverWithData<R>(
    override val listenToEvents: List<ComponentId>,
    override val module: GearyModule,
) : ObserverEventsBuilder<ObserverContextWithData<R>>() {
    override val mustHoldData: Boolean = true

    override fun provideContext(entity: GearyEntity, data: Any?) = ObserverContextWithData(entity, data as R)
}

abstract class ObserverEventsBuilder<Context> : ExecutableObserver<Context> {
    abstract val listenToEvents: List<ComponentId>
    abstract val module: GearyModule
    abstract val mustHoldData: Boolean

    abstract fun provideContext(entity: GearyEntity, data: Any?): Context

    fun involving(components: EntityType): ObserverBuilder<Context> {
        return ObserverBuilder(this, components)
    }

    inline fun <reified A : Any> involving(size1: QueryShorthands.Size1? = null): ObserverBuilder<Context> {
        return ObserverBuilder(this, entityTypeOf(componentId<A>()))
    }

    inline fun <reified A : Any, reified B : Any> involving(size2: QueryShorthands.Size2? = null): ObserverBuilder<Context> {
        return ObserverBuilder(this, entityTypeOf(cId<A>(), cId<B>()))
    }

    inline fun <reified A : Any, reified B : Any, reified C : Any> involving(size3: QueryShorthands.Size3? = null): ObserverBuilder<Context> {
        return ObserverBuilder(this, entityTypeOf(cId<A>(), cId<B>(), cId<C>()))
    }

    fun involvingAny(): ObserverBuilder<Context> {
        return ObserverBuilder(this, entityTypeOf())
    }

    override fun filter(vararg queries: Query) = involvingAny().filter(*queries)

    override fun exec(handle: Context.() -> Unit) = involvingAny().exec { handle() }

    fun <Q : ShorthandQuery> involving(involvingQuery: Q) =
        QueryInvolvingObserverBuilder(
            involvingQuery,
            ObserverBuilder(this, involvingQuery.involves, listOf(involvingQuery))
        )
}
