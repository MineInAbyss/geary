package com.mineinabyss.geary.engine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

interface AsyncScheduler {
    fun runSafely(scope: CoroutineScope, job: Job)
}
