package com.mineinabyss.geary.systems.query

import com.mineinabyss.geary.datatypes.Record
import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.accessors.AccessorHolder
import com.mineinabyss.geary.systems.accessors.FamilyMatching
import com.soywiz.kds.iterators.fastForEachWithIndex
import kotlin.reflect.KProperty

/**com.mineinabyss.geary.ecs.engine.iteration.accessors
 * @property matchedArchetypes A set of archetypes which have been matched to this query.
 */
abstract class Query : AccessorHolder() {
    @PublishedApi
    internal val matchedArchetypes: MutableSet<Archetype> = mutableSetOf()

    @PublishedApi
    internal var registered: Boolean = false

//    override fun iterator(): Iterator<TargetScope> {
//        val items = mutableListOf<TargetScope>()
//        fastForEach { items += it }
//        return items.iterator()
//    }

    inline fun fastForEach(crossinline run: (Record) -> Unit) {
        if (!registered) {
            geary.queryManager.trackQuery(this)
        }
        val matched = matchedArchetypes.toList()
//        val sizes = matched.map { it.size - 1 }
        matched.fastForEachWithIndex { i, archetype ->
            archetype.isIterating = true
            val upTo = archetype.entities.size
            for(entityIndex in 0 until upTo) {
                run(Record(archetype, entityIndex))
            }
            archetype.isIterating = false
        }
    }

    // FamilyMatching automatically constructs the family
    operator fun Family.provideDelegate(thisRef: GearyQuery, property: KProperty<*>) =
        mutableFamily.add(this)

    operator fun <T : FamilyMatching> T.provideDelegate(
        thisRef: Any,
        prop: KProperty<*>
    ): T {
        mutableFamily.add(family)
        return this
    }
}
