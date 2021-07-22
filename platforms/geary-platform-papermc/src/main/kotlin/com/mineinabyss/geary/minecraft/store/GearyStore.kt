package com.mineinabyss.geary.minecraft.store

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
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

    public fun encode(entity: GearyEntity): ByteArray

    public fun write(entity: GearyEntity, bytes: ByteArray = encode(entity))

    public fun decode(entity: GearyEntity, uuid: UUID)

    public fun read(uuid: UUID): ByteArray?

    public fun read(entity: GearyEntity): ByteArray? {
        return read(entity.get<UUID>() ?: return null)
    }
}
