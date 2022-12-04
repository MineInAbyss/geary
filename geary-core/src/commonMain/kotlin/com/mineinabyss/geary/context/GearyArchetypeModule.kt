package com.mineinabyss.geary.context

import com.mineinabyss.geary.datatypes.maps.HashTypeMap
import com.mineinabyss.geary.datatypes.maps.TypeMap
import com.mineinabyss.geary.engine.*
import com.mineinabyss.geary.engine.archetypes.*
import com.mineinabyss.geary.engine.archetypes.operations.ArchetypeMutateOperations
import com.mineinabyss.geary.engine.archetypes.operations.ArchetypeReadOperations
import com.mineinabyss.geary.engine.impl.UnorderedSystemProvider
import com.mineinabyss.geary.serialization.Formats
import com.mineinabyss.geary.serialization.Serializers
import kotlin.time.Duration.Companion.milliseconds

public val archetypes: GearyArchetypeModule = TODO()

public class GearyArchetypeModule : GearyModule {
    override val logger: Nothing = TODO()
    public val records: TypeMap = HashTypeMap()
    override val queryManager: ArchetypeQueryManager = ArchetypeQueryManager()

    override val components: Components = Components()
    override val serializers: Serializers = Serializers()
    override val formats: Formats = Formats()

    override val engine: ArchetypeEngine = ArchetypeEngine(100.milliseconds)
    public override val eventRunner: ArchetypeEventRunner = ArchetypeEventRunner(records)
    override val systems: SystemProvider = UnorderedSystemProvider(queryManager)

    public val archetypeProvider: ArchetypeProvider = SimpleArchetypeProvider(eventRunner, queryManager, records)

    override val read: EntityReadOperations = ArchetypeReadOperations(records)
    override val write: EntityMutateOperations = ArchetypeMutateOperations(records, archetypeProvider)
    override val entityProvider: EntityProvider = EntityByArchetypeProvider(records, archetypeProvider)
    override val componentProvider: ComponentProvider = ComponentAsEntityProvider(entityProvider)
}
