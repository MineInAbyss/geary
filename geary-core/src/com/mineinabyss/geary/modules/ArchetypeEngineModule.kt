package com.mineinabyss.geary.modules

import co.touchlab.kermit.Logger
import com.mineinabyss.dependencies.*
import com.mineinabyss.geary.datatypes.maps.ArrayTypeMap
import com.mineinabyss.geary.datatypes.maps.SynchronizedArrayTypeMap
import com.mineinabyss.geary.engine.*
import com.mineinabyss.geary.engine.archetypes.*
import com.mineinabyss.geary.engine.archetypes.operations.ArchetypeMutateOperations
import com.mineinabyss.geary.engine.archetypes.operations.ArchetypeReadOperations
import com.mineinabyss.geary.helpers.async.AsyncCatcher
import com.mineinabyss.geary.helpers.async.IgnoringAsyncCatcher
import com.mineinabyss.geary.observers.ArchetypeEventRunner
import com.mineinabyss.geary.observers.EventRunner
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

internal object ArchetypesModules {
    // Module for any classes without other dependencies as parameters
    val noDependencies = module("noDependencies") {
        single<ArrayTypeMap> { if (get<Boolean>("useSynchronized")) SynchronizedArrayTypeMap() else ArrayTypeMap() }
        single<AsyncCatcher> { IgnoringAsyncCatcher() }
    }

    val archetypes = module("archetypes") {
        import(submodule(noDependencies))
        single { ArchetypeQueryManager() }.and<QueryManager>()
        single<ArchetypeProvider> { new(::SimpleArchetypeProvider) }
    }

    val entities = module("entities") {
        import(submodule(archetypes))
        single { EntityByArchetypeProvider(get("reuseIDsAfterRemoval"), get(), get(), get()) }.and<EntityProvider>()
    }

    val components = module("components") {
        import(submodule(entities))
        single<ComponentProvider> { new(::ComponentAsEntityProvider) }
        single { new(::Components) }
    }

    val core = module("core") {
        import(submodule(components))
        single { new(::ArchetypeReadOperations) }.and<EntityReadOperations>()
        single<Pipeline> { new(::PipelineImpl) }
        single { new(::EntityInfoReader) }
    }

    val engine = module("engine") {
        import(submodule(core))
        single { ArchetypeEngine(get(), get(), get("tickDuration"), get("engineThread")) }.and<Engine>()
    }
}

fun ArchetypeEngineModule(
    logger: Logger? = Logger,
    tickDuration: Duration = 50.milliseconds,
    reuseIDsAfterRemoval: Boolean = true,
    useSynchronized: Boolean = false,
    beginTickingOnStart: Boolean = true,
    engineThread: () -> CoroutineContext = { (CoroutineScope(Dispatchers.Default) + CoroutineName("Geary Engine")).coroutineContext },
) = module("geary-archetypes") {
    if (logger != null) single<Logger> { logger }
    single("tickDuration") { tickDuration }
    single("reuseIDsAfterRemoval") { reuseIDsAfterRemoval }
    single("useSynchronized") { useSynchronized }
    single("beginTickingOnStart") { beginTickingOnStart }
    single("engineThread") { engineThread }

    import(submodule(ArchetypesModules.engine))
    single<EventRunner> { new(::ArchetypeEventRunner) }
    single { ArchetypeMutateOperations(get(), get(), get(), get(), get(), getOrNull("asyncCatcher.write") ?: get()) }
        .and<EntityMutateOperations>()
    single { new(::EntityRemove) }
    single<EngineInitializer> { ArchetypeEngineInitializer(get("beginTickingOnStart"), get(), get()) }

    single<Geary> { Geary(di) }
    get<EntityProvider>()
    get<EngineInitializer>().init()
}
