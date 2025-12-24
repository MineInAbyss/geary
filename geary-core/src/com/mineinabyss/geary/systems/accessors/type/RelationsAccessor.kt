package com.mineinabyss.geary.systems.accessors.type

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.EntityId
import com.mineinabyss.geary.datatypes.Relation
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.systems.accessors.FamilyMatching
import com.mineinabyss.geary.systems.accessors.ReadOnlyAccessor

class RelationsAccessor(
    val kind: ComponentId,
    val target: EntityId,
) : ReadOnlyAccessor<List<Relation>>, FamilyMatching {
    override val family = family { hasRelation(kind, target) }

    private var cachedRelations = emptyList<Relation>()

    override fun load(archetype: Archetype) {
        cachedRelations = archetype.getRelations(kind, target)
    }

    override fun get(row: Int): List<Relation> {
        return cachedRelations
    }
}
