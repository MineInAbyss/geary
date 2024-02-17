package com.mineinabyss.geary.systems.query

import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.systems.accessors.AccessorOperations
import com.mineinabyss.geary.systems.accessors.FamilyMatching
import kotlin.reflect.KProperty

abstract class Query : AccessorOperations() {
    @PublishedApi
    internal var currArchetype: Archetype? = null
    @PublishedApi
    internal var currEntityIndex: Int = 0

    val target: QueriedEntity = TODO()

    private val props: MutableMap<KProperty<*>, Family> = mutableMapOf()
    fun buildQuery(): Family = family {
        for (family in props.values) {
            add(family)
        }
    }

    /** Automatically matches families for any accessor that's supposed to match a family. */
    operator fun <T : FamilyMatching> T.provideDelegate(
        thisRef: Any,
        prop: KProperty<*>
    ): T {
        family?.let { props[prop] = it }
        return this
    }
}
