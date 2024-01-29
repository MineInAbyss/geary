package com.mineinabyss.geary.modules

import co.touchlab.kermit.Logger
import com.mineinabyss.geary.addons.GearyPhase
import com.mineinabyss.geary.datatypes.maps.ArrayTypeMap
import com.mineinabyss.geary.datatypes.maps.TypeMap
import com.mineinabyss.geary.engine.Components
import com.mineinabyss.geary.engine.PipelineImpl
import com.mineinabyss.geary.engine.archetypes.*
import com.mineinabyss.geary.engine.archetypes.operations.ArchetypeMutateOperations
import com.mineinabyss.geary.engine.archetypes.operations.ArchetypeReadOperations
import com.mineinabyss.idofront.di.DI
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

val archetypes: ArchetypeEngineModule by DI.observe()

open class ArchetypeEngineModule(
    tickDuration: Duration = 50.milliseconds,
) : GearyModule {
    override val logger = Logger.withTag("Geary")
    override val queryManager = ArchetypeQueryManager()

    override val engine = ArchetypeEngine(tickDuration)
    override val eventRunner = ArchetypeEventRunner()
    override val pipeline = PipelineImpl()

    override val read = ArchetypeReadOperations()
    override val write = ArchetypeMutateOperations()
    override val entityProvider = EntityByArchetypeProvider()
    override val componentProvider = ComponentAsEntityProvider()
    override val defaults: Defaults = Defaults()

    open val records: TypeMap = ArrayTypeMap()
    open val archetypeProvider: ArchetypeProvider = SimpleArchetypeProvider()

    override val components by lazy { Components() }

    companion object : GearyModuleProviderWithDefault<ArchetypeEngineModule> {
        override fun default(): ArchetypeEngineModule {
            return ArchetypeEngineModule()
        }

        override fun init(module: ArchetypeEngineModule) {
            DI.add<ArchetypeEngineModule>(module)
            DI.add<GearyModule>(module)
            module.componentProvider.createComponentInfo()
        }

        override fun start(module: ArchetypeEngineModule) {
            module {
                on(GearyPhase.ENABLE) {
                    module.engine.start()
                }
            }
            geary.pipeline.runStartupTasks()
        }
    }
}
