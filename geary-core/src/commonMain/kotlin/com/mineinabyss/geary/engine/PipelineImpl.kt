package com.mineinabyss.geary.engine

import com.mineinabyss.geary.addons.GearyPhase

class PipelineImpl : Pipeline {
    //    internal val loadingPrefabs = mutableListOf<Entity>()
    private val actions = sortedMapOf<GearyPhase, MutableList<() -> Unit>>()

    fun add(phase: GearyPhase, action: () -> Unit) {
        if (actions.isEmpty()) scheduleLoadTasks()

        actions.getOrPut(phase) { mutableListOf() }.add(action)
    }

    override fun intercept(phase: GearyPhase, block: () -> Unit) {
        TODO("Not yet implemented")
    }
}
