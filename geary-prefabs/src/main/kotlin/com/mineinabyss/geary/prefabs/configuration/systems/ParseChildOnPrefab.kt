package com.mineinabyss.geary.prefabs.configuration.systems

import com.mineinabyss.geary.ecs.accessors.TargetScope
import com.mineinabyss.geary.ecs.accessors.building.get
import com.mineinabyss.geary.autoscan.AutoScan
import com.mineinabyss.geary.ecs.api.annotations.Handler
import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.api.systems.GearyListener
import com.mineinabyss.geary.ecs.components.EntityName
import com.mineinabyss.geary.ecs.entities.addParent
import com.mineinabyss.geary.prefabs.configuration.components.ChildOnPrefab
import com.mineinabyss.geary.prefabs.configuration.components.ChildrenOnPrefab

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
