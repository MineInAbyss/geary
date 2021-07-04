package com.mineinabyss.geary.ecs.query

import com.mineinabyss.geary.ecs.engine.iteration.ArchetypeIterator
import com.mineinabyss.geary.ecs.engine.iteration.QueryResult

public class QueryIterator(
    public val query: Query
) : Iterator<QueryResult> {
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

    override fun next(): QueryResult {
        return archetypeIterator!!.next()
    }
}
