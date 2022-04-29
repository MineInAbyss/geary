package com.mineinabyss.geary.systems.query

import com.mineinabyss.geary.context.GearyContext
import com.mineinabyss.geary.context.GearyContextKoin
import com.mineinabyss.geary.datatypes.GearyComponent
import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.systems.accessors.Accessor
import com.mineinabyss.geary.systems.accessors.AccessorHolder
import com.mineinabyss.geary.systems.accessors.TargetScope
import com.mineinabyss.geary.systems.accessors.types.ComponentAccessor
import com.mineinabyss.geary.systems.accessors.types.DirectAccessor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlin.reflect.KProperty

/**com.mineinabyss.geary.ecs.engine.iteration.accessors
 * @property matchedArchetypes A set of archetypes which have been matched to this query.
 */
public abstract class GearyQuery : Iterable<TargetScope>, AccessorHolder(), GearyContext by GearyContextKoin() {
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
        forEach(run = { items += it })
        return items.iterator()
    }

    internal inline fun forEach(crossinline run: (TargetScope) -> Unit) {
        if (!registered) {
            queryManager.trackQuery(this)
            registered = true
        }
        val sizes = matchedArchetypes.map { it.size }
        matchedArchetypes.forEachIndexed { i, archetype ->
            archetype.isIterating = true
            archetype.iteratorFor(this@GearyQuery).forEach(upTo = sizes[i] - 1) { targetScope ->
                run(targetScope)
            }
            archetype.cleanup()
            archetype.isIterating = false
        }
    }

    @Suppress("unused") // Specifically
    @Deprecated("Likely trying to access component off entity", ReplaceWith("entity.get()"))
    protected inline fun <reified T : GearyComponent> TargetScope.get(): ComponentAccessor<T> =
        error("Cannot change query at runtime")

    @Suppress("UNCHECKED_CAST")
    public operator fun <T> Accessor<T>.getValue(thisRef: TargetScope, property: KProperty<*>): T =
        access(thisRef)

    @Suppress("UNCHECKED_CAST")
    public operator fun Family.provideDelegate(thisRef: GearyQuery, property: KProperty<*>): DirectAccessor<Family> =
        _family.add(this).run { DirectAccessor(family) }
}

public operator fun <T : GearyQuery, R> T.invoke(run: T.() -> R): R = run { run() }
