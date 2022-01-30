package com.mineinabyss.geary.ecs.query

import com.mineinabyss.geary.ecs.accessors.Accessor
import com.mineinabyss.geary.ecs.accessors.AccessorHolder
import com.mineinabyss.geary.ecs.accessors.TargetScope
import com.mineinabyss.geary.ecs.accessors.types.ComponentAccessor
import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.systems.QueryManager
import com.mineinabyss.geary.ecs.engine.Archetype
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.runBlocking
import org.koin.core.component.inject
import kotlin.reflect.KProperty

/**com.mineinabyss.geary.ecs.engine.iteration.accessors
 * @property matchedArchetypes A set of archetypes which have been matched to this query.
 */
public abstract class Query : Iterable<TargetScope>, AccessorHolder() {
    private val queryManager by inject<QueryManager>()

    @PublishedApi
    internal val matchedArchetypes: MutableSet<Archetype> = mutableSetOf()

    private var registered = false

    public fun flow(): Flow<TargetScope> {
        return channelFlow {
            forEach { targetScope ->
                send(targetScope)
            }
        }
    }

    override fun iterator(): Iterator<TargetScope> {
        val items = mutableListOf<TargetScope>()
        runBlocking {
            forEach(run = { items += it })
        }
        return items.iterator()
    }

    internal suspend inline fun forEach(crossinline run: suspend (TargetScope) -> Unit) {
        if (!registered) {
            queryManager.trackQuery(this)
            registered = true
        }
        for (archetype in matchedArchetypes) {
            archetype.iteratorFor(this@Query).forEach { targetScope ->
                run(targetScope)
            }
        }
    }

    @Suppress("unused") // Specifically
    @Deprecated("Likely trying to access component off entity", ReplaceWith("entity.get()"))
    protected inline fun <reified T : GearyComponent> TargetScope.get(): ComponentAccessor<T> =
        error("Cannot change query at runtime")

    @Suppress("UNCHECKED_CAST")
    public operator fun <T> Accessor<T>.getValue(thisRef: TargetScope, property: KProperty<*>): T =
        thisRef.data[index] as T

}

public suspend operator fun <T : Query, R> T.invoke(run: suspend T.() -> R): R = run { run() }
