package com.mineinabyss.geary.engine.archetypes

import com.mineinabyss.geary.engine.AsyncScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

//TODO implement safe running properly
public class PostTickScheduler: AsyncScheduler {
    private val queuedCleanup = mutableSetOf<Archetype>()
    private val runningAsyncJobs = mutableSetOf<Job>()
    private var iterationJob: Job? = null
    private val safeDispatcher = Dispatchers.Default.limitedParallelism(1)


    internal fun scheduleRemove(archetype: Archetype) {
        queuedCleanup += archetype
    }

    override fun runSafely(scope: CoroutineScope, job: Job) {
//        launch(safeDispatcher) {
//            iterationJob?.join()
//            runningAsyncJobs += job
//            job.invokeOnCompletion {
//                launch(safeDispatcher) {
//                    runningAsyncJobs -= job
//                }
//            }
//            job.start()
//        }
    }

}
