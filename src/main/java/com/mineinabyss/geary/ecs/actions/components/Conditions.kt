package com.mineinabyss.geary.ecs.actions.components

import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.autoscan.AutoscanComponent
import com.mineinabyss.geary.ecs.api.conditions.GearyCondition
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.serialization.FlatSerializer
import com.mineinabyss.geary.ecs.serialization.FlatWrap
import com.mineinabyss.geary.ecs.serialization.Formats
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

/**
 * A component that holds a map of component serial names to a list of [GearyCondition]s that need to be met for those
 * components to be considered active and iterated over.
 */
@Serializable(with = ConditionsSerializer::class)
@AutoscanComponent
public class Conditions(
    override val wrapped: Map<String, List<GearyCondition>>
) : FlatWrap<Map<String, List<GearyCondition>>> {
    private val wrappedClasses: Map<GearyComponentId, List<GearyCondition>> =
        wrapped.mapKeys { (serialName, _) -> componentId(Formats.getClassFor(serialName)) }

    /**
     * Whether the conditions for a list of [component classes][componentIds] are met for an [entity].
     * If a component doesn't have any conditions registered here, we consider them met.
     */
    public fun conditionsMet(componentIds: IntArray, entity: GearyEntity): Boolean {
        return componentIds.all { component ->
            wrappedClasses[component]?.all { it.conditionsMet(entity) } ?: true
        }
    }
}

private object ConditionsSerializer : FlatSerializer<Conditions, Map<String, List<GearyCondition>>>(
    "geary:conditions", serializer(), { Conditions(it) }
)
