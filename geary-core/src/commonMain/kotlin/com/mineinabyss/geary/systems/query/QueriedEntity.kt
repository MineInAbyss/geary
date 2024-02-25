package com.mineinabyss.geary.systems.query

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.Record
import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.modules.archetypes
import com.mineinabyss.geary.systems.accessors.Accessor
import com.mineinabyss.geary.systems.accessors.AccessorOperations
import com.mineinabyss.geary.systems.accessors.FamilyMatching

open class EventQueriedEntity : QueriedEntity(cacheAccessors = false)
open class QueriedEntity(
    override val cacheAccessors: Boolean
) : AccessorOperations() {

    internal val extraFamilies: MutableList<Family> = mutableListOf()

    internal val props: MutableMap<String, Accessor> = mutableMapOf()

    @PublishedApi
    internal val accessors: MutableSet<Accessor> = mutableSetOf()

    fun buildFamily(): Family.Selector.And = family {
        accessors
            .filterIsInstance<FamilyMatching>()
            .mapNotNull { it.family }
            .union(extraFamilies)
            .forEach(::add)
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

    @UnsafeAccessors
    val unsafeEntity: Entity
        get() {
            val entity = archetype.getEntity(row)
            if (!delegated) {
                delegate = archetypes.records[entity]
            }
            delegated = true
            return entity
        }
}
