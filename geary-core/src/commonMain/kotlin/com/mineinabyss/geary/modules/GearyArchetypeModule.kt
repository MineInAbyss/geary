package com.mineinabyss.geary.modules

import co.touchlab.kermit.Logger
import com.mineinabyss.ding.DI
import com.mineinabyss.ding.DIContext
import com.mineinabyss.geary.addons.dsl.GearyAddon
import com.mineinabyss.geary.datatypes.maps.HashTypeMap
import com.mineinabyss.geary.datatypes.maps.TypeMap
import com.mineinabyss.geary.engine.*
import com.mineinabyss.geary.engine.archetypes.*
import com.mineinabyss.geary.engine.archetypes.operations.ArchetypeMutateOperations
import com.mineinabyss.geary.engine.archetypes.operations.ArchetypeReadOperations
import com.mineinabyss.geary.engine.impl.UnorderedSystemProvider
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

val archetypes: GearyArchetypeModule by DI.observe()

class GearyArchetypeModule(
    tickDuration: Duration = 50.milliseconds,
) : GearyModule {
    override val logger: Logger = Logger.withTag("Geary")
    override val queryManager = ArchetypeQueryManager()

    override val components: Components = Components()

    override val engine: ArchetypeEngine = ArchetypeEngine(tickDuration)
    override val eventRunner: ArchetypeEventRunner = ArchetypeEventRunner()
    override val addons: DIContext = DIContext()
    override val pipeline: Pipeline get() = TODO("Not yet implemented")
    override val systems: SystemProvider = UnorderedSystemProvider()

    override val read: EntityReadOperations = ArchetypeReadOperations()
    override val write: EntityMutateOperations = ArchetypeMutateOperations()
    override val entityProvider: EntityProvider = EntityByArchetypeProvider()
    override val componentProvider: ComponentProvider = ComponentAsEntityProvider()

    val records: TypeMap = HashTypeMap()
    val archetypeProvider: ArchetypeProvider = SimpleArchetypeProvider()

    override fun inject() {
        DI.add<GearyModule>(this)
        DI.add(this)
    }

    override fun start() {
        engine.start()
    }
}
