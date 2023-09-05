package com.mineinabyss.geary.prefabs.configuration.systems

import com.mineinabyss.geary.components.EntityName
import com.mineinabyss.geary.components.relations.NoInherit
import com.mineinabyss.geary.datatypes.Records
import com.mineinabyss.geary.datatypes.UnsafeAccessors
import com.mineinabyss.geary.helpers.addParent
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.prefabs.configuration.components.ChildOnPrefab
import com.mineinabyss.geary.prefabs.configuration.components.ChildrenOnPrefab
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.systems.Listener
import com.mineinabyss.geary.systems.accessors.Pointers


class ParseChildOnPrefab : Listener() {
    private var Pointers.child by get<ChildOnPrefab>().removable().whenSetOnTarget()

    @OptIn(UnsafeAccessors::class)
    override fun Records.handle() {
        entity {
            addParent(target.entity)
            setAll(child!!.components)
        }
        child = null
    }
}

class ParseChildrenOnPrefab : Listener() {
    private var Pointers.children by get<ChildrenOnPrefab>().removable().whenSetOnTarget()

    @OptIn(UnsafeAccessors::class)
    override fun Records.handle() {
        children!!.nameToComponents.forEach { (name, components) ->
            entity {
                set(EntityName(name))
                set(Prefab())
                addParent(target.entity)
                addRelation<NoInherit, Prefab>()
                setAll(components)
            }
        }
        children = null
    }
}
