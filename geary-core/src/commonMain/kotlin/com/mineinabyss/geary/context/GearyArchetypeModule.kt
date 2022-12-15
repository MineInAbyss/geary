package com.mineinabyss.geary.context

import com.mineinabyss.ding.DI
import com.mineinabyss.geary.datatypes.maps.HashTypeMap
import com.mineinabyss.geary.datatypes.maps.TypeMap
import com.mineinabyss.geary.engine.*
import com.mineinabyss.geary.engine.archetypes.*
import com.mineinabyss.geary.engine.archetypes.operations.ArchetypeMutateOperations
import com.mineinabyss.geary.engine.archetypes.operations.ArchetypeReadOperations
import com.mineinabyss.geary.engine.impl.UnorderedSystemProvider
import com.mineinabyss.geary.serialization.Formats
import com.mineinabyss.geary.serialization.Serializers
import java.util.logging.Logger
import kotlin.time.Duration

public val archetypes: IArchetypeModule by DI.observe()

public class GearyArchetypeModule(
    tickDuration: Duration,
) : GearyModule, TransitiveModule {
    override val submodules = listOf(geary)

    override val logger: Logger = Logger.getLogger("geary")
    override val queryManager = ArchetypeQueryManager()

    override val components: Components = Components()
    override val serializers: Serializers = Serializers()
    override val formats: Formats = Formats()

    override val engine: ArchetypeEngine = ArchetypeEngine(tickDuration)
    override val eventRunner: ArchetypeEventRunner = ArchetypeEventRunner()
    override val systems: SystemProvider = UnorderedSystemProvider()

    override val read: EntityReadOperations = ArchetypeReadOperations()
    override val write: EntityMutateOperations = ArchetypeMutateOperations()
    override val entityProvider: EntityProvider = EntityByArchetypeProvider()
    override val componentProvider: ComponentProvider = ComponentAsEntityProvider()
}

public interface IArchetypeModule {
    public val records: TypeMap
    public val archetypeProvider: ArchetypeProvider
}

public class ArchetypeModule : IArchetypeModule {
    override val records: TypeMap = HashTypeMap()
    override val archetypeProvider: ArchetypeProvider = SimpleArchetypeProvider()
}
