package com.mineinabyss.geary.modules

import co.touchlab.kermit.Logger
import com.mineinabyss.geary.addons.ApplicationEnvironment
import com.mineinabyss.geary.addons.ApplicationFactory
import com.mineinabyss.geary.addons.GearyPhase
import com.mineinabyss.geary.addons.dependencies
import com.mineinabyss.geary.datatypes.maps.ArrayTypeMap
import com.mineinabyss.geary.datatypes.maps.SynchronizedArrayTypeMap
import com.mineinabyss.geary.engine.Components
import com.mineinabyss.geary.engine.PipelineImpl
import com.mineinabyss.geary.engine.archetypes.*
import com.mineinabyss.geary.engine.archetypes.operations.ArchetypeMutateOperations
import com.mineinabyss.geary.engine.archetypes.operations.ArchetypeReadOperations
import com.mineinabyss.geary.observers.ArchetypeEventRunner
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.di.DIContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

val archetypes: ArchetypeEngineModule by DI.observe()

data class ArchetypeEngineConfig(
    var tickDuration: Duration = 50.milliseconds,
    var logger: Logger = Logger.withTag("Geary"),
    var key: String = "geary",
    var reuseIDsAfterRemoval: Boolean = true,
    var useSynchronized: Boolean = false,
)

open class ArchetypeEngineModule(
    config: ArchetypeEngineConfig,
) : GearyModule {
    override val di: DIContext = DI.scoped(config.key)
    override val environment: ApplicationEnvironment = ApplicationEnvironment()
    override val logger = config.logger
    override val queryManager = ArchetypeQueryManager()

    override val engine = ArchetypeEngine(config.tickDuration)
    override val eventRunner = ArchetypeEventRunner()
    override val pipeline = PipelineImpl()

    open val records: ArrayTypeMap = if (config.useSynchronized) SynchronizedArrayTypeMap() else ArrayTypeMap()

    override val read = ArchetypeReadOperations()
    override val write = ArchetypeMutateOperations()
    override val entityProvider = EntityByArchetypeProvider(config.reuseIDsAfterRemoval)
    override val componentProvider = ComponentAsEntityProvider()

    open val archetypeProvider: ArchetypeProvider = SimpleArchetypeProvider()

    override val components by lazy { Components() }

    override fun start() {
        onPhase(GearyPhase.ENABLE) {
            engine.start()
        }

        geary.pipeline.runStartupTasks()
    }

    companion object : ApplicationFactory<ArchetypeEngineModule, ArchetypeEngineConfig> {
        fun init(module: ArchetypeEngineModule) = module.run {
            dependencies {
                add<ArchetypeEngineModule>(module)
                add<GearyModule>(module)
            }
            entityProvider.init(module.records, module.archetypeProvider.rootArchetype)
            write.init(module.records)
            componentProvider.createComponentInfo()
        }


        override fun create(configure: ArchetypeEngineConfig.() -> Unit): ArchetypeEngineModule {
            val config = ArchetypeEngineConfig().apply(configure)
            return ArchetypeEngineModule(config).apply {
                init(this)
            }
        }
    }
}
