package com.mineinabyss.geary.ecs.conditions

import com.mineinabyss.geary.ecs.api.conditions.GearyCondition
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.helper.Components
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * > geary:entity
 *
 * Does checks against the components of an entity. For example, whether it has a list of components.
 */
@Serializable
@SerialName("geary:entity")
public class EntityComponentConditions(
    @SerialName("has")
    public val names: Set<String> = emptySet(),
): GearyCondition() {
    @Transient
    private val _components = Components(names)

    override fun GearyEntity.check(): Boolean =
        hasAll(_components.classes)
}