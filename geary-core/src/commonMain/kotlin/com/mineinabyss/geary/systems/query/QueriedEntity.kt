package com.mineinabyss.geary.systems.query

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.modules.archetypes
import com.mineinabyss.geary.systems.accessors.Accessor
import com.mineinabyss.geary.systems.accessors.AccessorOperations
import com.mineinabyss.geary.systems.accessors.FamilyMatching

open class QueriedEntity(
    override val cacheAccessors: Boolean
) : AccessorOperations() {

    internal val extraFamilies: MutableList<Family> = mutableListOf()

    internal val props: MutableMap<String, Accessor> = mutableMapOf()

    fun buildFamily(): Family.Selector.And = family {
        accessors
            .filterIsInstance<FamilyMatching>()
            .mapNotNull { it.family }
            .union(extraFamilies)
            .forEach(::add)
    }

    @UnsafeAccessors
    internal inline fun reset(row: Int, archetype: Archetype) {
        this.row = row
        this.archetype = archetype
        cachingAccessors.forEach { it.updateCache(archetype) }
    }

    @PublishedApi
    @UnsafeAccessors
    internal var archetype = archetypes.archetypeProvider.rootArchetype

    @PublishedApi
    @UnsafeAccessors
    internal var row = 0

    private var delegate: GearyEntity? = null

    @UnsafeAccessors
    val unsafeEntity: Entity
        get() = this.archetype.getEntity(row)
}
