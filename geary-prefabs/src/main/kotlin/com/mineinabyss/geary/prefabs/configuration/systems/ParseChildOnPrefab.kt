package com.mineinabyss.geary.prefabs.configuration.systems

import com.mineinabyss.geary.annotations.AutoScan
import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.components.EntityName
import com.mineinabyss.geary.components.NoInherit
import com.mineinabyss.geary.helpers.addParent
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.prefabs.configuration.components.ChildOnPrefab
import com.mineinabyss.geary.prefabs.configuration.components.ChildrenOnPrefab
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.TargetScope

@AutoScan
class ParseChildOnPrefab : GearyListener() {
    private val TargetScope.child by added<ChildOnPrefab>()

    @Handler
    private fun TargetScope.convertToRelation() {
        entity {
            addParent(entity)
            setAll(child.components)
        }
        entity.remove<ChildOnPrefab>()
    }
}

@AutoScan
class ParseChildrenOnPrefab : GearyListener() {
    private val TargetScope.children by added<ChildrenOnPrefab>()

    @Handler
    private fun TargetScope.convertToRelation() {
        children.nameToComponents.forEach { (name, components) ->
            entity {
                set(EntityName(name))
                set(Prefab())
                addParent(entity)
                setRelation(Prefab::class, NoInherit)
                setAll(components)
            }
        }
        entity.remove<ChildrenOnPrefab>()
    }
}
