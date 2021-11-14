package com.mineinabyss.geary.ecs.query

import com.mineinabyss.geary.ecs.accessors.ResultScope
import com.mineinabyss.geary.ecs.engine.ArchetypeIterator

public class QueryIterator(
    public val query: Query
) : Iterator<ResultScope> {
    private val archetypes = query.matchedArchetypes.toList().iterator()

    override fun hasNext(): Boolean {
        if (archetypeIterator?.hasNext() == true) return true
        if (!archetypes.hasNext()) return false

        while (archetypeIterator?.hasNext() == false) {
            if (!archetypes.hasNext()) return false
            archetypeIterator = nextIterator()
        }

        return true
    }

    private var archetypeIterator: ArchetypeIterator? = null

    init {
        if (hasNext()) archetypeIterator = nextIterator()
    }

    private fun nextIterator(): ArchetypeIterator {
        return archetypes.next().iteratorFor(query)
    }

    override fun next(): ResultScope {
        return archetypeIterator!!.next()
    }
}
