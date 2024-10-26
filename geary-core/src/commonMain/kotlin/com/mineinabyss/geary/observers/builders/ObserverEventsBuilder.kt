package com.mineinabyss.geary.observers.builders

import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.engine.ComponentProvider
import com.mineinabyss.geary.engine.id
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.observers.Observer
import com.mineinabyss.geary.systems.query.Query
import com.mineinabyss.geary.systems.query.QueryShorthands
import com.mineinabyss.geary.systems.query.ShorthandQuery

data class ObserverWithoutData(
    override val listenToEvents: List<ComponentId>,
    override val world: Geary,
    override val onBuild: (Observer) -> Unit,
) : ObserverEventsBuilder<ObserverContext>() {
    override val mustHoldData: Boolean = false
    inline fun <reified R> or() = copy(listenToEvents = listenToEvents + comp.id<R>())

    private val context = object : ObserverContext {
        override var entity: Entity = world.NO_ENTITY
    }

    override fun provideContext(entity: EntityId, data: Any?): ObserverContext {
        context.entity = GearyEntity(entity, world)
        return context
    }
}

data class ObserverWithData<R>(
    override val listenToEvents: List<ComponentId>,
    override val world: Geary,
    override val onBuild: (Observer) -> Unit,
) : ObserverEventsBuilder<ObserverContextWithData<R>>() {
    override val mustHoldData: Boolean = true

    private val context = object : ObserverContextWithData<R> {
        var data: R? = null
        override val event: R get() = data!!
        override var entity: Entity = world.NO_ENTITY
    }

    override fun provideContext(entity: EntityId, data: Any?): ObserverContextWithData<R> {
        context.entity = GearyEntity(entity, world)
        context.data = data as R
        return context
    }
}

abstract class ObserverEventsBuilder<Context> : ExecutableObserver<Context> {
    abstract val world: Geary
    abstract val listenToEvents: List<ComponentId>
    abstract val mustHoldData: Boolean
    abstract val onBuild: (Observer) -> Unit

    val comp: ComponentProvider get() = world.componentProvider

    abstract fun provideContext(entity: EntityId, data: Any?): Context

    fun involving(components: EntityType): ObserverBuilder<Context> {
        return ObserverBuilder(comp, this, components)
    }

    inline fun <reified A : Any> involving(size1: QueryShorthands.Size1? = null): ObserverBuilder<Context> {
        return ObserverBuilder(comp, this, entityTypeOf(comp.id<A>()))
    }

    inline fun <reified A : Any, reified B : Any> involving(size2: QueryShorthands.Size2? = null): ObserverBuilder<Context> {
        return ObserverBuilder(comp, this, entityTypeOf(comp.id<A>(), comp.id<B>()))
    }

    inline fun <reified A : Any, reified B : Any, reified C : Any> involving(size3: QueryShorthands.Size3? = null): ObserverBuilder<Context> {
        return ObserverBuilder(comp, this, entityTypeOf(comp.id<A>(), comp.id<B>(), comp.id<C>()))
    }

    inline fun <reified A : Any, reified B : Any, reified C : Any, reified D : Any> involving(size4: QueryShorthands.Size4? = null): ObserverBuilder<Context> {
        return ObserverBuilder(
            comp,
            this,
            entityTypeOf(comp.id<A>(), comp.id<B>(), comp.id<C>(), comp.id<D>())
        )
    }

    inline fun <reified A : Any, reified B : Any, reified C : Any, reified D : Any, reified E : Any> involving(size5: QueryShorthands.Size5? = null): ObserverBuilder<Context> {
        return ObserverBuilder(
            comp,
            this,
            entityTypeOf(comp.id<A>(), comp.id<B>(), comp.id<C>(), comp.id<D>(), comp.id<E>())
        )
    }

    fun involvingAny(): ObserverBuilder<Context> {
        return ObserverBuilder(comp, this, entityTypeOf())
    }

    override fun filter(vararg queries: Query) = involvingAny().filter(*queries)

    override fun exec(handle: Context.() -> Unit) = involvingAny().exec { handle() }

    fun <Q : ShorthandQuery> involving(involvingQuery: Q) =
        QueryInvolvingObserverBuilder(
            involvingQuery,
            ObserverBuilder(comp, this, involvingQuery.involves, listOf(involvingQuery))
        )
}
