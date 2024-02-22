package com.mineinabyss.geary.prefabs.configuration.systems

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.components.EntityName
import com.mineinabyss.geary.components.relations.NoInherit
import com.mineinabyss.geary.helpers.addParent
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.prefabs.configuration.components.ChildOnPrefab
import com.mineinabyss.geary.prefabs.configuration.components.ChildrenOnPrefab
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.systems.listener
import com.mineinabyss.geary.systems.query.ListenerQuery


@OptIn(UnsafeAccessors::class)
fun createParseChildOnPrefabListener() = geary.listener(object : ListenerQuery() {
    var child by get<ChildOnPrefab>().removable()
    override fun ensure() = event.anySet(::child)
}).exec {
    entity {
        addParent(entity)
        setAll(child!!.components)
    }
    child = null
}

@OptIn(UnsafeAccessors::class)
fun createParseChildrenOnPrefabListener() = geary.listener(object : ListenerQuery() {
    var children by get<ChildrenOnPrefab>().removable()
    override fun ensure() = event.anySet(::children)
}).exec {
    children!!.nameToComponents.forEach { (name, components) ->
        entity {
            set(EntityName(name))
            set(Prefab())
            addParent(entity)
            addRelation<NoInherit, Prefab>()
            setAll(components)
        }
    }
    children = null
}
