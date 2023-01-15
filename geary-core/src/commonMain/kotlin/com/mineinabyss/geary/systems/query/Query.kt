package com.mineinabyss.geary.systems.query

import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.datatypes.Component
import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.systems.accessors.Accessor
import com.mineinabyss.geary.systems.accessors.AccessorHolder
import com.mineinabyss.geary.systems.accessors.TargetScope
import com.mineinabyss.geary.systems.accessors.types.ComponentAccessor
import com.mineinabyss.geary.systems.accessors.types.DirectAccessor
import com.soywiz.kds.iterators.fastForEachWithIndex
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlin.reflect.KProperty

/**com.mineinabyss.geary.ecs.engine.iteration.accessors
 * @property matchedArchetypes A set of archetypes which have been matched to this query.
 */
abstract class Query : AccessorHolder(), Iterable<TargetScope> {
    @PublishedApi
    internal val matchedArchetypes: MutableSet<Archetype> = mutableSetOf()

    @PublishedApi
    internal var registered: Boolean = false

    fun flow(): Flow<TargetScope> {
        return channelFlow {
            forEach { targetScope ->
                send(targetScope)
            }
        }
    }

    override fun iterator(): Iterator<TargetScope> {
        val items = mutableListOf<TargetScope>()
        fastForEach { items += it }
        return items.iterator()
    }

    inline fun fastForEach(crossinline run: (TargetScope) -> Unit) {
        if (!registered) {
            geary.queryManager.trackQuery(this)
        }
        val matched = matchedArchetypes.toList()
        val sizes = matched.map { it.size - 1 }
        matched.fastForEachWithIndex { i, archetype ->
            archetype.isIterating = true
            archetype.iteratorFor(this@Query).forEach(upTo = sizes[i]) { targetScope ->
                run(targetScope)
            }
            archetype.isIterating = false
        }
    }

    @Suppress("unused") // Specifically
    @Deprecated("Likely trying to access component off entity", ReplaceWith("entity.get()"))
    protected inline fun <reified T : Component> TargetScope.get(): ComponentAccessor<T> =
        error("Cannot change query at runtime")

    operator fun <T> Accessor<T>.getValue(thisRef: TargetScope, property: KProperty<*>): T =
        access(thisRef)

    operator fun Family.provideDelegate(thisRef: GearyQuery, property: KProperty<*>): DirectAccessor<Family> =
        _family.add(this).run { DirectAccessor(family) }
}
