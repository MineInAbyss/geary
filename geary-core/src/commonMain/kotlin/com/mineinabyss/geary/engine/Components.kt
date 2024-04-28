package com.mineinabyss.geary.engine

import com.mineinabyss.geary.components.CouldHaveChildren
import com.mineinabyss.geary.components.KeepArchetype
import com.mineinabyss.geary.components.relations.ChildOf
import com.mineinabyss.geary.components.relations.InstanceOf
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.observers.events.*

class Components {
    val any: ComponentId = componentId<Any>()
    val suppressRemoveEvent = componentId<SuppressRemoveEvent>()
    val couldHaveChildren = componentId<CouldHaveChildren>()
    val onAdd = componentId<OnAdd>()
    val onSet = componentId<OnSet>()
    val onFirstSet = componentId<OnFirstSet>()
    val onUpdate = componentId<OnUpdate>()
    val onRemove = componentId<OnRemove>()
    val onExtend = componentId<OnExtend>()
    val onEntityRemoved = componentId<OnEntityRemoved>()
    val childOf = componentId<ChildOf>()
    val instanceOf = componentId<InstanceOf>()
    val keepArchetype = componentId<KeepArchetype>()
}
