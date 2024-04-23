package com.mineinabyss.geary.prefabs.configuration.systems

import com.mineinabyss.geary.components.relations.NoInherit
import com.mineinabyss.geary.events.types.OnSet
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.configuration.components.InheritPrefabs
import com.mineinabyss.geary.prefabs.configuration.components.InstancesOnPrefab
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.systems.builders.observe

fun GearyModule.createParseInstancesOnPrefabListener() = observe<OnSet>()
    .involving<InstancesOnPrefab, PrefabKey>()
    .exec { (instances, prefabKey) ->
        entity.addRelation<NoInherit, InstancesOnPrefab>()
        instances.nameToComponents.forEach { (name, components) ->
            entity {
                set(PrefabKey.of(prefabKey.namespace, name))
                set(Prefab())
                set(InheritPrefabs(setOf(prefabKey)))
                addRelation<NoInherit, Prefab>()
                setAll(components)
            }
            logger.d("Created instance $name of prefab $prefabKey")
        }
    }
