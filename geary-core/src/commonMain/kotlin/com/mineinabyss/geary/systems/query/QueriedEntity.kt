package com.mineinabyss.geary.systems.query

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.datatypes.EntityId
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.engine.archetypes.ArchetypeProvider
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.get
import com.mineinabyss.geary.systems.accessors.Accessor
import com.mineinabyss.geary.systems.accessors.AccessorOperations
import com.mineinabyss.geary.systems.accessors.FamilyMatching
import kotlin.jvm.JvmField

open class QueriedEntity(
    final override val world: Geary,
    final override val cacheAccessors: Boolean,
) : AccessorOperations(), Geary by world {
    @PublishedApi
    @UnsafeAccessors
    @JvmField
    internal var archetype = world.get<ArchetypeProvider>().rootArchetype

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
    @JvmField
    internal var row = 0

    @UnsafeAccessors
    val unsafeEntity: EntityId
        get() = this.archetype.getEntity(row)
}
