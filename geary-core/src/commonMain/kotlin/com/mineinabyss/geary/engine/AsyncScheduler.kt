package com.mineinabyss.geary.engine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

public interface AsyncScheduler {
    public fun runSafely(scope: CoroutineScope, job: Job)
}
