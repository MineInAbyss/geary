package com.mineinabyss.geary.prefabs.configuration.systems

import com.mineinabyss.geary.annotations.AutoScan
import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.components.EntityName
import com.mineinabyss.geary.helpers.addParent
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.prefabs.configuration.components.ChildOnPrefab
import com.mineinabyss.geary.prefabs.configuration.components.ChildrenOnPrefab
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.TargetScope

@AutoScan
public class ParseChildOnPrefab : GearyListener() {
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
public class ParseChildrenOnPrefab : GearyListener() {
    private val TargetScope.children by added<ChildrenOnPrefab>()

    @Handler
    private fun TargetScope.convertToRelation() {
        children.nameToComponents.forEach { (name, components) ->
            entity {
                addParent(entity)
                set(EntityName(name))
                setAll(components)
            }
        }
        entity.remove<ChildrenOnPrefab>()
    }
}
