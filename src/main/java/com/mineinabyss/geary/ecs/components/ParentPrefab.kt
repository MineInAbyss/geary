package com.mineinabyss.geary.ecs.components

import com.mineinabyss.geary.ecs.GearyEntity

/**
 * A prefab that holds an entity representing the prefab this entity came from.
 *
 * TODO this sounds like we just wanna be able to add entities to entities :eyes:
 */
public class ParentPrefab(
    public val entity: GearyEntity
)
