package com.mineinabyss.geary.events.queries

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.systems.query.*

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
    override val mustHoldData: Boolean = false

    override fun provideContext(entity: GearyEntity, data: Any?) = ObserverContextWithData(entity, data as R)
}

abstract class ObserverEventsBuilder<Context> {
    abstract val listenToEvents: List<ComponentId>
    abstract val module: GearyModule
    abstract val mustHoldData: Boolean

    abstract fun provideContext(entity: GearyEntity, data: Any?): Context

    inline fun <reified A : Any> involving(size1: QueryShorthands.Size1? = null): ObserverBuilder<ShorthandQuery1<A>, Context> {
        return ObserverBuilder(this, Query.of<A>())
    }

    inline fun <reified A : Any, reified B : Any> involving(size2: QueryShorthands.Size2? = null): ObserverBuilder<ShorthandQuery2<A, B>, Context> {
        return ObserverBuilder(this, Query.of<A, B>())
    }

    inline fun <reified A : Any, reified B : Any,  reified C : Any> involving(size3: QueryShorthands.Size3? = null): ObserverBuilder<ShorthandQuery3<A, B, C>, Context> {
        return ObserverBuilder(this, Query.of<A, B, C>())
    }

    fun involvingAny(): ObserverBuilder<Query, Context> {
        return ObserverBuilder(this, Query.any())
    }

    fun <P : Query> filter(otherQuery: P) = FilteredObserverBuilder(involvingAny(), otherQuery)

    fun exec(handle: Context.() -> Unit) = involvingAny().exec { handle() }
}
