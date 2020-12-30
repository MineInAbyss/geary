package com.mineinabyss.geary.ecs.actions.components

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.actions.conditions.Condition
import com.mineinabyss.geary.ecs.engine.ComponentClass
import com.mineinabyss.geary.ecs.serialization.FlatSerializer
import com.mineinabyss.geary.ecs.serialization.FlatWrap
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

/**
 * A component that holds a map of component serial names to a list of [Condition]s that need to be met for those
 * components to be considered active and iterated over.
 */
@Serializable(with = ConditionsSerializer::class)
public class Conditions(
    override val wrapped: Map<String, Condition>
) : GearyComponent, FlatWrap<Map<String, Condition>> {
    private val wrappedClasses: Map<KClass<out GearyComponent>, Condition> =
        wrapped.mapKeys { it.key.toComponentClass() }

    /**
     * Whether the conditions for a list of [component classes][kClasses] are met for an [entity].
     * If a component doesn't have any conditions registered here, we consider them met.
     */
    public fun conditionsMet(kClasses: Array<out ComponentClass>, entity: GearyEntity): Boolean {
        return kClasses.all {
            wrappedClasses[it]?.conditionsMet(entity) ?: true
        }
    }
}

private object ConditionsSerializer : FlatSerializer<Conditions, Map<String, Condition>>(
    "geary:conditions", serializer(), { Conditions(it) }
)
