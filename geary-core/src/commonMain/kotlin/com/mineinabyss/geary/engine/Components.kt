package com.mineinabyss.geary.engine

import com.mineinabyss.geary.components.relations.Persists
import com.mineinabyss.geary.datatypes.GearyComponentId
import com.mineinabyss.geary.helpers.componentId

public class Components {
    public val any: GearyComponentId = componentId<Any>()
    public val persists: GearyComponentId = componentId<Persists>()
}
