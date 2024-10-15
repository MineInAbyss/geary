package com.mineinabyss.geary.systems.accessors.type

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.EntityId
import com.mineinabyss.geary.datatypes.Relation
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.engine.ComponentProvider
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.systems.accessors.Accessor
import com.mineinabyss.geary.systems.accessors.FamilyMatching
import com.mineinabyss.geary.systems.accessors.ReadOnlyAccessor
import com.mineinabyss.geary.systems.query.Query

class RelationsAccessor(
    comp: ComponentProvider,
    override val originalAccessor: Accessor?,
    val kind: ComponentId,
    val target: EntityId,
) : ReadOnlyAccessor<List<Relation>>, FamilyMatching {
    override val family = family(comp) { hasRelation(kind, target) }

    private var cachedRelations = emptyList<Relation>()
    private var cachedArchetype: Archetype? = null

    @OptIn(UnsafeAccessors::class)
    override fun get(query: Query): List<Relation> {
        val archetype = query.archetype
        if (archetype != cachedArchetype) {
            cachedArchetype = archetype
            cachedRelations = archetype.getRelations(kind, target)
        }

        return cachedRelations
    }
}
