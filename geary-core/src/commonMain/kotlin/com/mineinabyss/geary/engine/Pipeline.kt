package com.mineinabyss.geary.engine

import com.mineinabyss.geary.addons.GearyPhase

interface Pipeline {
    fun intercept(phase: GearyPhase, block: () -> Unit)
}
