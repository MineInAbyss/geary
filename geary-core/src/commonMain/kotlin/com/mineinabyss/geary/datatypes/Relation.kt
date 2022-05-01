package com.mineinabyss.geary.datatypes

import com.mineinabyss.geary.context.GearyContextKoin
import com.mineinabyss.geary.helpers.componentId
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.reflect.KClass

/**
 * A combination of two [GearyComponentId]s into one that represents a relation between
 * the two. Used for "adding a component to another component."
 *
 * Data of the [value]'s type is stored under the relation's full [id] in archetypes.
 * The [key] points to another component this relation references.
 *
 * ```
 * [value] bits: 0x00FFFFFF00000000
 * [key]   bits: 0xFF000000FFFFFFFF
 * ```
 *
 * @property value The part of the relation which determines the data type of the full relation.
 * @property key The part of the relation that points to another component on the entity.
 *
 */
@Serializable
@JvmInline
public value class Relation private constructor(
    public val id: GearyComponentId
) : Comparable<Relation> {
    public val value: RelationValueId get() = RelationValueId(id and RELATION_VALUE_MASK shr 32)
    public val key: GearyComponentId get() = id and RELATION_KEY_MASK and RELATION.inv()

    override fun compareTo(other: Relation): Int = id.compareTo(other.id)

    override fun toString(): String = "${key.readableString()} to ${value.id.readableString()}"

    public companion object {
        public fun of(
            key: GearyComponentId,
            value: RelationValueId
        ): Relation = Relation(
            (value.id shl 32 and RELATION_VALUE_MASK)
                    or (key and RELATION_KEY_MASK)
                    or RELATION
        )

        public fun of(key: GearyComponentId, value: GearyComponentId): Relation =
            of(key, RelationValueId(value))

        public fun of(key: KClass<*>, value: KClass<*>): Relation = GearyContextKoin {
            of(componentId(key), componentId(value))
        }

        public inline fun <reified K : GearyComponent, reified V : GearyComponent> of(): Relation = GearyContextKoin {
            of(componentId<K>(), componentId<V>())
        }

        /**
         * Creates a relation from an id that is assumed to be valid. Use this to avoid boxing Relation because of
         * the nullable type on [toRelation].
         */
        public fun of(id: GearyComponentId): Relation = Relation(id)
    }
}

/**
 * Data of this parent's type is stored under a [Relation]'s full [id][Relation.id] in archetypes.
 *
 * ```
 * Parent bits:     0x00FFFFFF00000000
 * ```
 */
@JvmInline
public value class RelationValueId(public val id: GearyComponentId)

public fun GearyComponentId.toRelation(): Relation? = Relation.of(this).takeIf { isRelation() }
