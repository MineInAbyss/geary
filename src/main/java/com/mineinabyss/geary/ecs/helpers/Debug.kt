package com.mineinabyss.geary.ecs.helpers

import com.mineinabyss.geary.ecs.components.ComponentName
import com.mineinabyss.geary.ecs.components.get
import com.mineinabyss.geary.ecs.engine.GearyEngine
import com.mineinabyss.geary.ecs.geary

public fun GearyEngine.countEntitiesOfType(type: String): Int {
    bitsets.forEach { (t, u) ->
        if (geary(t).get<ComponentName>()?.name == type)
            return u.cardinality()
    }
    return 0
}
