package com.mineinabyss.geary.modules

import co.touchlab.kermit.Logger
import co.touchlab.kermit.mutableLoggerConfigInit
import co.touchlab.kermit.platformLogWriter
import com.mineinabyss.dependencies.DI
import com.mineinabyss.dependencies.DIContext
import com.mineinabyss.dependencies.DIScope
import com.mineinabyss.dependencies.get
import com.mineinabyss.geary.datatypes.maps.ArrayTypeMap
import com.mineinabyss.geary.engine.*
import com.mineinabyss.geary.engine.archetypes.EntityRemove
import com.mineinabyss.geary.observers.EventRunner

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
interface Geary : WorldScoped {
    override val logger: Logger get() = get<Logger>()

    // By default, we always get the latest instance of deps, the Impl class gets them once for user
    // access where the engine isn't expected to be reloaded (ex. like it might be in tests)
    val eventRunner: EventRunner
    val read: EntityReadOperations
    val infoReader: EntityInfoReader
    val write: EntityMutateOperations
    val queryManager: QueryManager
    val pipeline: Pipeline
    val entityProvider: EntityProvider
    val entityRemoveProvider: EntityRemove
    val components: Components
    val componentProvider: ComponentProvider
    val records: ArrayTypeMap
    val engine: GearyEngine
    val scope: DIScope

    fun configure(block: DIScope.() -> Unit): Geary {
        di.scope.block()
        return this
    }

    fun install(addon: DI.Module): DI {
        return scope.load(addon)
    }

    fun <T> install(addon: DI.ModuleWithConfig<T>, configure: T.() -> Unit) = scope.load(addon, configure)

    companion object : Logger(mutableLoggerConfigInit(listOf(platformLogWriter())), "Geary") {
        operator fun invoke(di: DI, logger: Logger? = null): Geary = Impl(di, logger)
    }

    class Impl(
        di: DI,
        logger: Logger? = null,
    ) : Geary {
        override val di: DIContext = di.di
        override val world: Geary = this@Impl
        override val logger: Logger = logger ?: super.logger
        override val eventRunner: EventRunner = get()
        override val read: EntityReadOperations = get()
        override val infoReader: EntityInfoReader = get()
        override val write: EntityMutateOperations = get()
        override val queryManager: QueryManager = get()
        override val pipeline: Pipeline = get()
        override val entityProvider: EntityProvider = get()
        override val entityRemoveProvider: EntityRemove = get()
        override val components: Components = get()
        override val componentProvider: ComponentProvider = get()
        override val records: ArrayTypeMap = get()
        override val engine: GearyEngine = get()
        override val scope: DIScope = get()
    }

    fun stringify() = get<String>("name")
}
