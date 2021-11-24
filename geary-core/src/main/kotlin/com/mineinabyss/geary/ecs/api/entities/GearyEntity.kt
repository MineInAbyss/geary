package com.mineinabyss.geary.ecs.api.entities

import com.mineinabyss.geary.ecs.api.ComponentClass
import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.GearyEntityId
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.api.relations.RelationDataType
import com.mineinabyss.geary.ecs.components.PersistingComponent
import com.mineinabyss.geary.ecs.engine.ENTITY_MASK
import com.mineinabyss.geary.ecs.engine.INSTANCEOF
import com.mineinabyss.geary.ecs.engine.withRole
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

/**
 * A wrapper around [GearyEntityId] that gets inlined to just a long (no performance degradation since no boxing occurs).
 *
 * Provides some useful functions so we aren't forced to go through [Engine] every time we want to do some things.
 */
@Serializable
@JvmInline
@Suppress("NOTHING_TO_INLINE")
public value class GearyEntity(public val id: GearyEntityId) {
    public operator fun component1(): GearyEntityId = id
}
