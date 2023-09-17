package com.mineinabyss.geary.systems.query

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.accessors.AccessorHolder
import com.mineinabyss.geary.systems.accessors.FamilyMatching
import com.mineinabyss.geary.systems.accessors.Pointer
import com.soywiz.kds.iterators.fastForEachWithIndex
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**com.mineinabyss.geary.ecs.engine.iteration.accessors
 * @property matchedArchetypes A set of archetypes which have been matched to this query.
 */
abstract class Query : AccessorHolder() {
    @PublishedApi
    internal val matchedArchetypes: MutableSet<Archetype> = mutableSetOf()

    @PublishedApi
    internal var registered: Boolean = false

    val matchedEntities
        get(): List<GearyEntity> {
            registerIfNotRegistered()
            return matchedArchetypes.flatMap { it.entities }
        }

    fun registerIfNotRegistered() {
        if (!registered) {
            geary.queryManager.trackQuery(this)
        }
    }

    inline fun <T> toList(crossinline map: (Pointer) -> T): List<T> {
        val list = mutableListOf<T>()
        forEach { list.add(map(it)) }
        return list
    }

    /**
     * Quickly iterates over all matched entities, running [run] for each.
     *
     * Use [apply] on the query to use its accessors.
     * */
    inline fun forEach(crossinline run: (Pointer) -> Unit) {
        registerIfNotRegistered()
        val matched = matchedArchetypes.toList()
        matched.fastForEachWithIndex { i, archetype ->
            archetype.isIterating = true
            val upTo = archetype.size
            // TODO upTo isn't perfect for cases where entities may be added or removed in the same iteration
            for (entityIndex in 0 until upTo) {
                run(Pointer(archetype, entityIndex))
            }
            archetype.isIterating = false
        }
    }

    operator fun Family.provideDelegate(thisRef: GearyQuery, property: KProperty<*>): ReadOnlyProperty<Any, Family> {
        mutableFamily.add(this)
        return ReadOnlyProperty { thisRef, prop ->
            this@provideDelegate
        }
    }

    /** Automatically matches families for any accessor that's supposed to match a family. */
    operator fun <T : FamilyMatching> T.provideDelegate(
        thisRef: Any,
        prop: KProperty<*>
    ): T {
        family?.let { mutableFamily.add(it) }
        return this
    }
}
