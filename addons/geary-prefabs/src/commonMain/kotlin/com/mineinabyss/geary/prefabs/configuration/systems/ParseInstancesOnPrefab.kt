package com.mineinabyss.geary.prefabs.configuration.systems

import com.mineinabyss.geary.components.relations.NoInherit
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.configuration.components.InheritPrefabs
import com.mineinabyss.geary.prefabs.configuration.components.InstancesOnPrefab
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.systems.builders.observe
import com.mineinabyss.geary.systems.query.query

fun Geary.createParseInstancesOnPrefabListener() = observe<OnSet>()
    .involving(query<InstancesOnPrefab, PrefabKey>()).exec { (instances, prefabKey) ->
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
