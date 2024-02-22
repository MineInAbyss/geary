package com.mineinabyss.geary.systems.query

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.Record
import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.modules.archetypes
import kotlin.reflect.KProperty

class QueriedEntity {
    internal val extraFamilies: MutableList<Family> = mutableListOf()
    internal val props: MutableMap<KProperty<*>, Family.Selector> = mutableMapOf()

    fun buildFamily(): Family.Selector.And = family {
        for (family in props.values.union(extraFamilies)) {
            add(family)
        }
    }

    @PublishedApi
    internal var originalArchetype = archetypes.archetypeProvider.rootArchetype

    @PublishedApi
    internal var originalRow = 0

    @UnsafeAccessors
    val archetype: Archetype get() = if (delegated) delegate!!.archetype else originalArchetype
    val row: Int get() = if (delegated) delegate!!.row else originalRow

    private var delegate: Record? = null

    @PublishedApi
    internal var delegated = false

    internal fun delegateTo(record: Record) {
        delegated = true
        delegate = record
    }

    @UnsafeAccessors
    val entity: Entity
        get() {
            val entity = archetype.getEntity(row)
            if (!delegated) {
                delegate = archetypes.records[entity]
            }
            delegated = true
            return entity
        }

    @UnsafeAccessors
    operator fun component1(): Archetype = archetype

    operator fun component2(): Int = row
}
