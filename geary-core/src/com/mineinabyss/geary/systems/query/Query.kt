package com.mineinabyss.geary.systems.query

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.datatypes.EntityId
import com.mineinabyss.geary.datatypes.EntityType
import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.WorldScoped
import com.mineinabyss.geary.systems.accessors.Accessor
import com.mineinabyss.geary.systems.accessors.FamilyMatching
import com.mineinabyss.geary.systems.accessors.type.ComponentAccessor

abstract class Query(world: Geary, family: Family?) : WorldScoped by world.newScope() {
    abstract val accessors: Set<Accessor<*>>
    val involves: EntityType = EntityType(accessors.filterIsInstance<ComponentAccessor<*>>().map { it.id })

    val family = family {
        accessors.filterIsInstance<FamilyMatching>().forEach { add(it.family) }
        if(family != null) add(family)
    }

    open fun load(archetype: Archetype) {}

    @UnsafeAccessors
    val unsafeEntity: EntityId
        get() = TODO() //this.archetype.getEntity(row)
}
