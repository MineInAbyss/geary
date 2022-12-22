package com.mineinabyss.geary.prefabs.configuration.systems

import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.components.EntityName
import com.mineinabyss.geary.components.relations.DontInherit
import com.mineinabyss.geary.helpers.addParent
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.prefabs.configuration.components.ChildOnPrefab
import com.mineinabyss.geary.prefabs.configuration.components.ChildrenOnPrefab
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.systems.Listener
import com.mineinabyss.geary.systems.accessors.TargetScope

class ParseChildOnPrefab : Listener() {
    private val TargetScope.child by onSet<ChildOnPrefab>()

    @Handler
    private fun TargetScope.convertToRelation() {
        entity {
            addParent(entity)
            setAll(child.components)
        }
        entity.remove<ChildOnPrefab>()
    }
}

class ParseChildrenOnPrefab : Listener() {
    private val TargetScope.children by onSet<ChildrenOnPrefab>()

    @Handler
    private fun TargetScope.convertToRelation() {
        children.nameToComponents.forEach { (name, components) ->
            entity {
                set(EntityName(name))
                set(Prefab())
                addParent(entity)
                addRelation<DontInherit, Prefab>()
                setAll(components)
            }
        }
        entity.remove<ChildrenOnPrefab>()
    }
}
