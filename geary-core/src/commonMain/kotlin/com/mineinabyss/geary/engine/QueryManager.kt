package com.mineinabyss.geary.engine

import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.systems.Listener
import com.mineinabyss.geary.systems.query.GearyQuery

public interface QueryManager {
    public fun trackEventListener(listener: Listener)
    public fun trackQuery(query: GearyQuery)

    public fun getEntitiesMatching(family: Family): List<Entity>
}
