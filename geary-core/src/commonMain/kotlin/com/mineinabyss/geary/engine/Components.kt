package com.mineinabyss.geary.engine

import com.mineinabyss.geary.components.CouldHaveChildren
import com.mineinabyss.geary.components.events.*
import com.mineinabyss.geary.components.relations.Persists
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.helpers.componentId

class Components {
    val any: ComponentId = componentId<Any>()
    val persists: ComponentId = componentId<Persists>()
    val suppressRemoveEvent = componentId<SuppressRemoveEvent>()
    val couldHaveChildren = componentId<CouldHaveChildren>()
    val addedComponent = componentId<AddedComponent>()
    val setComponent = componentId<SetComponent>()
    val updatedComponent = componentId<UpdatedComponent>()
    val entityRemoved = componentId<EntityRemoved>()
}
