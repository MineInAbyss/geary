package com.mineinabyss.geary.systems.builders

import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.observers.builders.ObserverWithData
import com.mineinabyss.geary.observers.builders.ObserverWithoutData
import com.mineinabyss.geary.systems.query.CachedQuery
import com.mineinabyss.geary.systems.query.Query


fun <T : Query> GearyModule.cache(
    query: T,
): CachedQuery<T> {
    return queryManager.trackQuery(query)
}

inline fun <reified T : Any> GearyModule.observe(): ObserverWithoutData {
    return ObserverWithoutData(listOf(componentId<T>()), this) {
        eventRunner.addObserver(it)
    }
}

inline fun <reified T : Any> GearyModule.observeWithData(): ObserverWithData<T> {
    return ObserverWithData(listOf(componentId<T>()), this) {
        eventRunner.addObserver(it)
    }
}

fun <T : Query> GearyModule.system(
    query: T
): SystemBuilder<T> {
    val defaultName = Throwable().stackTraceToString()
        .lineSequence()
        .drop(2) // First line error, second line is this function
        .first()
        .trim()
        .substringBeforeLast("(")
        .substringAfter("$")
        .substringAfter("Kt.")
        .substringAfter("create")

    return SystemBuilder(defaultName, query, pipeline)
}
