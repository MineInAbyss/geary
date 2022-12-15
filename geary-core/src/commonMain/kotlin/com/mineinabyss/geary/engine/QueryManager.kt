package com.mineinabyss.geary.engine

import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.systems.Listener
import com.mineinabyss.geary.systems.query.GearyQuery

interface QueryManager {
    fun trackEventListener(listener: Listener)
    fun trackQuery(query: GearyQuery)

    fun getEntitiesMatching(family: Family): List<Entity>
}
