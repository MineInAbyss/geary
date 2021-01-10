package com.mineinabyss.geary.ecs.helpers

import com.mineinabyss.geary.ecs.engine.GearyEngine

public fun GearyEngine.countEntitiesOfType(type: String): Int {
    bitsets.forEach { (t, u) ->
        if (t.simpleName == type)
            return u.cardinality()
    }
    return 0
}
