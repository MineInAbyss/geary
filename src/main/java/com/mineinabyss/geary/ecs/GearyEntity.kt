package com.mineinabyss.geary.ecs

import com.mineinabyss.geary.ecs.components.GearyPrefab
import com.mineinabyss.geary.ecs.components.addPersistingComponents
import com.mineinabyss.geary.ecs.components.get
import com.mineinabyss.geary.ecs.components.getPersistingComponents
import com.mineinabyss.geary.ecs.engine.Engine
import com.mineinabyss.geary.ecs.engine.entity
import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer


/** Type alias for entity IDs. */
@OptIn(ExperimentalUnsignedTypes::class)
public typealias GearyEntityId = ULong

/** Type alias for component IDs */
public typealias GearyComponentId = GearyEntityId

/**
 * Some extensions may want to represent existing classes as entities in the ECS without having to convert to and from
 * them. For instance, Mobzy implements this class for its custom mobs.
 */
public interface GearyEntity {
    public val gearyId: GearyEntityId

    public operator fun component1(): GearyEntityId = gearyId
}

public object GearyEntitySerializer : KSerializer<GearyEntity> {
    private val serializer = serializer<Set<@Contextual GearyComponent>>()
    override val descriptor: SerialDescriptor = serializer.descriptor

    override fun serialize(encoder: Encoder, value: GearyEntity) {
        encoder.encodeSerializableValue(serializer, value.getPersistingComponents())
    }

    override fun deserialize(decoder: Decoder): GearyEntity {
        return Engine.entity {
            addPersistingComponents(decoder.decodeSerializableValue(serializer))
        }
    }
}

/** The [GearyPrefab] associated with this entity. */
public val GearyEntity.type: GearyPrefab? get() = get<GearyPrefab>()

/** Remove this entity from the ECS. */
public fun GearyEntity.remove() {
    Engine.removeEntity(this)
}

/**
 * A wrapper around an integer id that allows us to use extension functions of [GearyEntity] but gets inlined to avoid
 * performance hits of boxing an integer.
 */
//TODO change name to reflect that it's not boxed anymore.
public inline class BoxedEntityID(override val gearyId: GearyEntityId) : GearyEntity

/** Gets the entity associated with [id] and runs code on it. */
public inline fun geary(id: GearyEntityId, run: GearyEntity.() -> Unit): GearyEntity =
    BoxedEntityID(id).apply(run)

/** Gets the entity associated with [id]. */
@Suppress("NOTHING_TO_INLINE")
public inline fun geary(id: GearyEntityId): GearyEntity = BoxedEntityID(id)

@Suppress("NOTHING_TO_INLINE")
public inline fun geary(id: Long): GearyEntity = geary(id.toULong())

