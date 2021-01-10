package com.mineinabyss.geary.ecs.conditions

import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.actions.components.toComponentClass
import com.mineinabyss.geary.ecs.components.hasAll
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("entity")
public class ComponentConditions(
    @SerialName("has")
    public val components: Set<String> = emptySet(),
): GearyCondition {
    //TODO this is getting boilerplatey, reused from ComponentAction
    private val componentClasses by lazy { components.map { it.toComponentClass() } }

    override fun conditionsMet(entity: GearyEntity): Boolean =
        entity.hasAll(componentClasses)
}
