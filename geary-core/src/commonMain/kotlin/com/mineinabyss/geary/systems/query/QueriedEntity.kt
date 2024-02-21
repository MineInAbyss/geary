package com.mineinabyss.geary.systems.query

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.modules.archetypes
import kotlin.reflect.KProperty

class QueriedEntity {
    internal val extraFamilies: MutableList<Family> = mutableListOf()

    internal val props: MutableMap<KProperty<*>, Family> = mutableMapOf()

    @PublishedApi
    internal var currArchetype: Archetype = archetypes.archetypeProvider.rootArchetype

    @PublishedApi
    internal var currRow: Int = 0

    @UnsafeAccessors
    val entity: GearyEntity get() = currArchetype.getEntity(currRow)

    fun buildFamily(): Family.Selector.And = family {
        for (family in props.values.union(extraFamilies)) {
            add(family)
        }
    }
}
