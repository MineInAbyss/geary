package com.mineinabyss.geary.prefabs.configuration.components

import com.mineinabyss.geary.components.EntityName
import com.mineinabyss.geary.datatypes.Component
import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import com.mineinabyss.geary.serialization.serializers.PolymorphicListAsMapSerializer
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer

/**
 * > geary:children
 *
 * A component that will add a list of named children to this entity.
 *
 * The keys will be used to set an extra [EntityName] component.
 */
@Serializable(with = ChildrenOnPrefab.Serializer::class)
class ChildrenOnPrefab(
    val nameToComponents: Map<String, List<@Polymorphic Component>>
) {
    class Serializer : InnerSerializer<Map<String, List<Component>>, ChildrenOnPrefab>(
        "geary:children",
        MapSerializer(String.serializer(), PolymorphicListAsMapSerializer.of(PolymorphicSerializer(Component::class))),
        { ChildrenOnPrefab(it) },
        { it.nameToComponents },
    )
}
