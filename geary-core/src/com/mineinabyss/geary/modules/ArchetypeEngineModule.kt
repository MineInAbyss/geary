package com.mineinabyss.geary.modules

import co.touchlab.kermit.Logger
import com.mineinabyss.features.get
import com.mineinabyss.geary.datatypes.maps.ArrayTypeMap
import com.mineinabyss.geary.datatypes.maps.SynchronizedArrayTypeMap
import com.mineinabyss.geary.engine.Components
import com.mineinabyss.geary.engine.EntityInfoReader
import com.mineinabyss.geary.engine.PipelineImpl
import com.mineinabyss.geary.engine.QueryManager
import com.mineinabyss.geary.engine.archetypes.*
import com.mineinabyss.geary.engine.archetypes.operations.ArchetypeMutateOperations
import com.mineinabyss.geary.engine.archetypes.operations.ArchetypeReadOperations
import com.mineinabyss.geary.helpers.async.AsyncCatcher
import com.mineinabyss.geary.helpers.async.IgnoringAsyncCatcher
import com.mineinabyss.geary.observers.ArchetypeEventRunner
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.plus
import org.kodein.di.*
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

internal object ArchetypesModules {
    // Module for any classes without other dependencies as parameters
    val noDependencies = DI.Module("noDependencies") {
        bindSingleton<ArrayTypeMap> { if (instance<Boolean>("useSynchronized")) SynchronizedArrayTypeMap() else ArrayTypeMap() }
        bindSingleton<AsyncCatcher> { IgnoringAsyncCatcher() }
    }

    val archetypes = DI.Module("archetypes") {
        import(noDependencies)
        bindSingleton { ArchetypeQueryManager() }
        delegate<QueryManager>().to<ArchetypeQueryManager>()
        bindSingletonOf(::SimpleArchetypeProvider)
    }

    val entities = DI.Module("entities") {
        import(archetypes)
        bindSingleton {
            EntityByArchetypeProvider(
                instance("reuseIDsAfterRemoval"),
                instance(),
                instance(),
                instance()
            )
        }
    }

    val components = DI.Module("components") {
        import(entities)
        bindSingletonOf(::ComponentAsEntityProvider)
        bindSingletonOf(::Components)
    }

    val core = DI.Module("core") {
        import(components)
        bindSingletonOf(::ArchetypeReadOperations)
        bindSingletonOf(::PipelineImpl)
        bindSingletonOf(::EntityInfoReader)
    }

    val engine = DI.Module("engine") {
        import(core)
        bindSingleton { ArchetypeEngine(get(), get(), instance("tickDuration"), instance("engineThread")) }
    }
}

fun ArchetypeEngineModule(
    logger: Logger? = Geary,
    tickDuration: Duration = 50.milliseconds,
    reuseIDsAfterRemoval: Boolean = true,
    useSynchronized: Boolean = false,
    beginTickingOnStart: Boolean = true,
    defaults: Defaults = Defaults(),
    engineThread: () -> CoroutineContext = { (CoroutineScope(Dispatchers.Default) + CoroutineName("Geary Engine")).coroutineContext },
) = GearyModule(
    DI.Module("archetypes") {
        if (logger != null) bindSingleton<Logger> { logger }
        import(ArchetypesModules.engine)
        bindSingletonOf(::ArchetypeEventRunner)
        bindSingleton {
            ArchetypeMutateOperations(
                get(),
                get(),
                get(),
                get(),
                get(),
                instanceOrNull("asyncCatcher.write") ?: get()
            )
        }
        bindSingletonOf(::EntityRemove)
        bindSingleton { ArchetypeEngineInitializer(instance("beginTickingOnStart"), get(), get()) }
        bindInstance("tickDuration") { tickDuration }
        bindInstance("reuseIDsAfterRemoval") { reuseIDsAfterRemoval }
        bindInstance("useSynchronized") { useSynchronized }
        bindInstance("beginTickingOnStart") { beginTickingOnStart }
        bindInstance("defaults") { defaults }
        bindInstance("engineThread") { engineThread }

        onReady {
            instance<EntityByArchetypeProvider>()
        }
    }
)
