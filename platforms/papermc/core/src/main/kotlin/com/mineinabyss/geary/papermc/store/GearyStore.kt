package com.mineinabyss.geary.papermc.store

import com.mineinabyss.geary.datatypes.GearyComponent
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.idofront.plugin.getService
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.builtins.SetSerializer
import java.util.*

public interface GearyStore {
    public companion object : GearyStore by getService() {
        public val componentsSerializer: KSerializer<Set<GearyComponent>> =
            SetSerializer(PolymorphicSerializer(GearyComponent::class))
    }

    public suspend fun encode(entity: GearyEntity): ByteArray

    public suspend fun write(entity: GearyEntity, bytes: ByteArray)

    public suspend fun decode(entity: GearyEntity, uuid: UUID)

    public fun read(uuid: UUID): ByteArray?

    public suspend fun read(entity: GearyEntity): ByteArray? {
        return read(entity.get<UUID>() ?: return null)
    }
}
