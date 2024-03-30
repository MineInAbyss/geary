package com.mineinabyss.geary.prefabs.configuration.systems

import com.mineinabyss.geary.components.relations.NoInherit
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.configuration.components.InheritPrefabs
import com.mineinabyss.geary.prefabs.configuration.components.InstancesOnPrefab
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.systems.builders.listener
import com.mineinabyss.geary.systems.query.ListenerQuery

fun GearyModule.createParseInstancesOnPrefabListener() = listener(object : ListenerQuery() {
    val instances by get<InstancesOnPrefab>()
    val prefabKey by get<PrefabKey>()
    override fun ensure() = event.anyFirstSet(::instances, ::prefabKey)
}).exec {
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
