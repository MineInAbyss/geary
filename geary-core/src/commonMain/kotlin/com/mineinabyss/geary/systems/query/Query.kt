package com.mineinabyss.geary.systems.query

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.accessors.AccessorHolder
import com.mineinabyss.geary.systems.accessors.FamilyMatching
import com.mineinabyss.geary.systems.accessors.Pointer
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

    val matchedEntities
        get(): List<GearyEntity> {
            registerIfUnregistered()
            return matchedArchetypes.flatMap { it.entities }
        }

    fun registerIfUnregistered() {
        if (!registered) {
            geary.queryManager.trackQuery(this)
        }
    }

    inline fun fastForEach(crossinline run: (Pointer) -> Unit) {
        registerIfUnregistered()
        val matched = matchedArchetypes.toList()
        matched.fastForEachWithIndex { i, archetype ->
            archetype.isIterating = true
            val upTo = archetype.size
            for (entityIndex in 0 until upTo) {
                run(Pointer(archetype, entityIndex))
            }
            archetype.isIterating = false
        }
    }

    operator fun Family.provideDelegate(thisRef: GearyQuery, property: KProperty<*>) =
        mutableFamily.add(this)

    /** Automatically matches families for any accessor that's supposed to match a family. */
    operator fun <T : FamilyMatching> T.provideDelegate(
        thisRef: Any,
        prop: KProperty<*>
    ): T {
        family?.let { mutableFamily.add(it) }
        return this
    }
}
