package com.mineinabyss.geary.systems.accessors.type

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.EntityId
import com.mineinabyss.geary.datatypes.Relation
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.engine.ComponentProvider
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.systems.accessors.Accessor
import com.mineinabyss.geary.systems.accessors.FamilyMatching
import com.mineinabyss.geary.systems.accessors.ReadOnlyAccessor
import com.mineinabyss.geary.systems.accessors.RelationWithData
import com.mineinabyss.geary.systems.query.Query

@OptIn(UnsafeAccessors::class)
class RelationsWithDataAccessor<K, T>(
    val comp: ComponentProvider,
    override val originalAccessor: Accessor?,
    val kind: ComponentId,
    val target: EntityId,
) : ReadOnlyAccessor<List<RelationWithData<K, T>>>, FamilyMatching {
    override val family = family { hasRelation(kind, target) }

    private var cachedRelations = emptyList<Relation>()
    private var cachedArchetype: Archetype? = null

    override fun get(query: Query): List<RelationWithData<K, T>> {
        val archetype = query.archetype
        if (archetype != cachedArchetype) {
            cachedArchetype = archetype
            cachedRelations = archetype.getRelations(kind, target)
        }

        @Suppress("UNCHECKED_CAST")
        return archetype.readRelationDataFor(
            query.row,
            kind,
            target,
            cachedRelations
        ) as List<RelationWithData<K, T>>
    }
}
