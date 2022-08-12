package com.mineinabyss.geary.engine

import com.mineinabyss.geary.components.relations.Persists
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.helpers.componentId

public class Components {
    public val any: ComponentId = componentId<Any>()
    public val persists: ComponentId = componentId<Persists>()
}
