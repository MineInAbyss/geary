package com.mineinabyss.geary.observers.builders

import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.helpers.NO_ENTITY
import com.mineinabyss.geary.helpers.cId
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.observers.Observer
import com.mineinabyss.geary.systems.query.Query
import com.mineinabyss.geary.systems.query.QueryShorthands
import com.mineinabyss.geary.systems.query.ShorthandQuery

data class ObserverWithoutData(
    override val listenToEvents: List<ComponentId>,
    override val module: GearyModule,
    override val onBuild: (Observer) -> Unit,
) : ObserverEventsBuilder<ObserverContext>() {
    override val mustHoldData: Boolean = false
    inline fun <reified R> or() = copy(listenToEvents = listenToEvents + componentId<R>())

    private val context = object : ObserverContext {
        override var entity: Entity = NO_ENTITY
    }

    override fun provideContext(entity: GearyEntity, data: Any?): ObserverContext {
        context.entity = entity
        return context
    }
}

data class ObserverWithData<R>(
    override val listenToEvents: List<ComponentId>,
    override val module: GearyModule,
    override val onBuild: (Observer) -> Unit,
) : ObserverEventsBuilder<ObserverContextWithData<R>>() {
    override val mustHoldData: Boolean = true

    private val context = object : ObserverContextWithData<R> {
        var data: R? = null
        override val event: R get() = data!!
        override var entity: Entity = NO_ENTITY
    }

    override fun provideContext(entity: GearyEntity, data: Any?): ObserverContextWithData<R> {
        context.entity = entity
        context.data = data as R
        return context
    }
}

abstract class ObserverEventsBuilder<Context> : ExecutableObserver<Context> {
    abstract val listenToEvents: List<ComponentId>
    abstract val module: GearyModule
    abstract val mustHoldData: Boolean
    abstract val onBuild: (Observer) -> Unit

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

    inline fun <reified A : Any, reified B : Any, reified C : Any, reified D : Any> involving(size4: QueryShorthands.Size4? = null): ObserverBuilder<Context> {
        return ObserverBuilder(this, entityTypeOf(cId<A>(), cId<B>(), cId<C>(), cId<D>()))
    }

    inline fun <reified A : Any, reified B : Any, reified C : Any, reified D : Any, reified E : Any> involving(size5: QueryShorthands.Size5? = null): ObserverBuilder<Context> {
        return ObserverBuilder(this, entityTypeOf(cId<A>(), cId<B>(), cId<C>(), cId<D>(), cId<E>()))
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
