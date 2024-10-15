package com.mineinabyss.geary.modules

import co.touchlab.kermit.Logger
import com.mineinabyss.geary.datatypes.maps.ArrayTypeMap
import com.mineinabyss.geary.engine.Components
import com.mineinabyss.geary.engine.PipelineImpl
import com.mineinabyss.geary.engine.archetypes.*
import com.mineinabyss.geary.engine.archetypes.operations.ArchetypeMutateOperations
import com.mineinabyss.geary.engine.archetypes.operations.ArchetypeReadOperations
import com.mineinabyss.geary.observers.ArchetypeEventRunner
import com.mineinabyss.idofront.di.DI
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class ArchetypeEngineModule(
    tickDuration: Duration = 50.milliseconds,
) : GearyModule {
    val records: ArrayTypeMap = ArrayTypeMap()
    override val logger = Logger.withTag("Geary")
    override val entityProvider = EntityByArchetypeProvider()
    override val defaults: Defaults = Defaults()

    override val componentProvider = ComponentAsEntityProvider(entityProvider, logger)
    override val components = Components(componentProvider)
    override val queryManager = ArchetypeQueryManager(componentProvider)
    override val read = ArchetypeReadOperations(components, records)
    override val pipeline = PipelineImpl(queryManager)
    override val engine = ArchetypeEngine(pipeline, logger, tickDuration)
    override val eventRunner = ArchetypeEventRunner(read, components.observer, componentProvider, records)
    override val write = ArchetypeMutateOperations(
        records,
        eventRunner = eventRunner,
        components = components,
        queryManager = queryManager,
    )
    override val entityRemoveProvider = EntityRemove(
        removedEntities = entityProvider.removedEntities,
        reader = read,
        write = write,
        records = records,
        components = components,
        eventRunner = eventRunner,
        queryManager = queryManager,
    )

    companion object : GearyModuleProviderWithDefault<ArchetypeEngineModule> {
        override fun default(): ArchetypeEngineModule {
            return ArchetypeEngineModule()
        }

        override fun init(module: ArchetypeEngineModule) {
            DI.add<ArchetypeEngineModule>(module)
            DI.add<GearyModule>(module)
            module.entityProvider.init(module.records, module.write.archetypeProvider.rootArchetype)
            module.componentProvider.createComponentInfo()
        }

        override fun start(module: ArchetypeEngineModule) {
            TODO("Fix")
//            module {
//                on(GearyPhase.ENABLE) {
//                    module.engine.start()
//                }
//            }
//            geary.pipeline.runStartupTasks()
        }
    }
}
