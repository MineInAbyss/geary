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
    val addedComponent = componentId<OnAdd>()
    val setComponent = componentId<OnSet>()
    val updatedComponent = componentId<OnUpdate>()
    val removedComponent = componentId<OnRemove>()
    val extendedEntity = componentId<OnExtend>()
    val entityRemoved = componentId<OnEntityRemoved>()
    val childOf = componentId<ChildOf>()
    val instanceOf = componentId<InstanceOf>()
    val keepArchetype = componentId<KeepArchetype>()
}
