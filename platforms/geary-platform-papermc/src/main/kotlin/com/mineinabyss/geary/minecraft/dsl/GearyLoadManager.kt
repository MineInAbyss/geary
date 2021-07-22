package com.mineinabyss.geary.minecraft.dsl

import com.mineinabyss.geary.minecraft.geary
import com.okkero.skedule.schedule

public object GearyLoadManager {
    private val actions = sortedMapOf<GearyLoadPhase, MutableList<() -> Unit>>()

    public fun add(phase: GearyLoadPhase, action: () -> Unit) {
        actions.getOrPut(phase, { mutableListOf() }).add(action)
    }

    private fun MutableList<() -> Unit>.runAll() = forEach { it() }

    internal fun onEnable() {
        geary.schedule {
            waitFor(1)
            actions[GearyLoadPhase.REGISTER_SERIALIZERS]?.runAll()
            actions[GearyLoadPhase.LOAD_PREFABS]?.runAll()
            waitFor(1)
            actions[GearyLoadPhase.ENABLE]?.runAll()
        }
    }
}
