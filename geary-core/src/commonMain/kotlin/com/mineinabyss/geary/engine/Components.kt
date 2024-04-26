package com.mineinabyss.geary.engine

import com.mineinabyss.geary.components.CouldHaveChildren
import com.mineinabyss.geary.components.KeepArchetype
import com.mineinabyss.geary.components.relations.ChildOf
import com.mineinabyss.geary.components.relations.InstanceOf
import com.mineinabyss.geary.components.relations.Persists
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.events.types.*
import com.mineinabyss.geary.helpers.componentId

class Components {
    val any: ComponentId = componentId<Any>()
    val persists: ComponentId = componentId<Persists>()
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
