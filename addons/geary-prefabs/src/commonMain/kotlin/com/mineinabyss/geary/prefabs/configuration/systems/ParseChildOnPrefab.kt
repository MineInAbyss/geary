package com.mineinabyss.geary.prefabs.configuration.systems

import com.mineinabyss.geary.components.EntityName
import com.mineinabyss.geary.components.relations.NoInherit
import com.mineinabyss.geary.events.types.OnSet
import com.mineinabyss.geary.helpers.addParent
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.prefabs.configuration.components.ChildOnPrefab
import com.mineinabyss.geary.prefabs.configuration.components.ChildrenOnPrefab
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.systems.builders.observe


fun GearyModule.createParseChildOnPrefabListener() = observe<OnSet>()
    .involving<ChildOnPrefab>()
    .exec { (child) ->
        entity {
            addParent(entity)
            setAll(child.components)
        }
        entity.remove<ChildOnPrefab>()
    }

fun GearyModule.createParseChildrenOnPrefabListener() = observe<OnSet>()
    .involving<ChildrenOnPrefab>()
    .exec { (children) ->
        children.nameToComponents.forEach { (name, components) ->
            entity {
                set(EntityName(name))
                set(Prefab())
                addParent(entity)
                addRelation<NoInherit, Prefab>()
                setAll(components)
            }
        }
        entity.remove<ChildrenOnPrefab>()
    }
