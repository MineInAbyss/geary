package com.mineinabyss.geary.ecs.conditions

import com.mineinabyss.geary.ecs.api.conditions.GearyCondition
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.helper.toComponentClasses
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * > geary:entity
 *
 * Does checks against the components of an entity. For example, whether it has a list of components.
 */
@Serializable
@SerialName("geary:entity_has")
public class EntityComponentConditions(
    @SerialName("components") public val componentNames: Set<String> = emptySet(),
): GearyCondition() {
    private val componentClasses by lazy { componentNames.toComponentClasses() }

    override fun GearyEntity.check(): Boolean =
        hasAll(componentClasses)
}


@Serializable
@SerialName("geary:entity_lacks")
public class EntityHasNoComponentConditions(
    @SerialName("components") public val componentNames: Set<String> = emptySet(),
) : GearyCondition() {
    private val componentClasses by lazy { componentNames.toComponentClasses() }

    override fun GearyEntity.check(): Boolean =
        !componentClasses.any { has(componentId(it)) }
}
