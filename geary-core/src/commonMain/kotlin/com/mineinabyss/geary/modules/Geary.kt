package com.mineinabyss.geary.modules

import co.touchlab.kermit.Logger
import co.touchlab.kermit.mutableLoggerConfigInit
import co.touchlab.kermit.platformLogWriter
import com.mineinabyss.geary.addons.dsl.Addon
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.observers.builders.ObserverWithData
import com.mineinabyss.geary.observers.builders.ObserverWithoutData
import com.mineinabyss.geary.systems.builders.SystemBuilder
import com.mineinabyss.geary.systems.query.CachedQuery
import com.mineinabyss.geary.systems.query.Query
import com.mineinabyss.idofront.di.DIContext
import kotlin.jvm.JvmName

/**
 * Root class for users to access geary functionality.
 *
 * Anything exposed to the user should be accessible here without
 * having to call functions on classes in the module,
 * it simply acts as a container for all dependencies.
 *
 * Any functions that modify the state of the engine modify it right away,
 * they are not scheduled for load phases like [GearySetup] is.
 */
open class Geary(
    val module: GearyModule,
    val context: DIContext,
    val logger: Logger = module.logger,
) {
    @JvmName("getAddon1")
    fun <T : Addon<*, Inst>, Inst> getAddon(addon: T): Inst = TODO()
    @JvmName("getAddon2")
    fun <T : Addon<*, Inst>, Inst> getAddon(addon: T?): Inst? = TODO()

    // Queries

    fun <T : Query> cache(
        query: T,
    ): CachedQuery<T> {
        return module.queryManager.trackQuery(query)
    }

    // TODO simple api for running queries in place without caching
    inline fun <T : Query> execute(
        query: T,
        run: T.() -> Unit = {},
    ): CachedQuery<T> {
        TODO()
    }

    inline fun <reified T : Any> observe(): ObserverWithoutData {
        return ObserverWithoutData(listOf(componentId<T>()), world = this) {
            module.eventRunner.addObserver(it)
        }
    }

    inline fun <reified T : Any> observeWithData(): ObserverWithData<T> {
        return ObserverWithData(listOf(componentId<T>()), world = this) {
            module.eventRunner.addObserver(it)
        }
    }

    fun <T : Query> system(
        query: T,
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

        return SystemBuilder(defaultName, query, module.pipeline)
    }

    companion object : Logger(mutableLoggerConfigInit(listOf(platformLogWriter())), "Geary")
}
