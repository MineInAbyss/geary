package com.mineinabyss.geary.engine

import com.mineinabyss.geary.components.relations.Persists
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.helpers.componentId

class Components {
    val any: ComponentId = componentId<Any>()
    val persists: ComponentId = componentId<Persists>()
}
