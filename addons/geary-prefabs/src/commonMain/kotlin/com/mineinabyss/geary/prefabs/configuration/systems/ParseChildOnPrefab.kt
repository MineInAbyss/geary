package com.mineinabyss.geary.prefabs.configuration.systems

import com.mineinabyss.geary.components.EntityName
import com.mineinabyss.geary.components.relations.NoInherit
import com.mineinabyss.geary.helpers.addParent
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.prefabs.configuration.components.ChildOnPrefab
import com.mineinabyss.geary.prefabs.configuration.components.ChildrenOnPrefab
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.systems.builders.observe
import com.mineinabyss.geary.systems.query.query


fun GearyModule.createParseChildOnPrefabListener() = observe<OnSet>()
    .involving(query<ChildOnPrefab>())
    .exec { (child) ->
        entity {
            addParent(entity)
            setAll(child.components)
        }
        entity.remove<ChildOnPrefab>()
    }

fun GearyModule.createParseChildrenOnPrefabListener() = observe<OnSet>()
    .involving(query<ChildrenOnPrefab>())
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
