package com.mineinabyss.geary.ecs.prefab

import com.mineinabyss.geary.ecs.accessors.TargetScope
import com.mineinabyss.geary.ecs.accessors.get
import com.mineinabyss.geary.ecs.api.autoscan.AutoScan
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.api.systems.GearyListener
import com.mineinabyss.geary.ecs.api.systems.Handler
import com.mineinabyss.geary.ecs.components.ChildOnPrefab
import com.mineinabyss.geary.ecs.components.ChildrenOnPrefab
import com.mineinabyss.geary.ecs.components.EntityName
import com.mineinabyss.geary.ecs.entities.addParent

@AutoScan
public class ParseChildOnPrefab : GearyListener() {
    private val TargetScope.child by get<ChildOnPrefab>()

    init {
        allAdded()
    }

    @Handler
    private fun TargetScope.convertToRelation() {
//        entity.parseEntity(child.new)
        Engine.entity {
            addParent(entity)
            setAll(child.new)
        }
        entity.remove<ChildOnPrefab>()
    }
}

@AutoScan
public class ParseChildrenOnPrefab : GearyListener() {
    private val TargetScope.children by get<ChildrenOnPrefab>()

    init {
        allAdded()
    }

    @Handler
    private fun TargetScope.convertToRelation() {
//        entity.parseEntity(child.new)
        children.nameToComponents.forEach { (name, components) ->
            Engine.entity {
                addParent(entity)
                set(EntityName(name))
                setAll(components)
            }
        }
        entity.remove<ChildrenOnPrefab>()
    }
}
