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
 * Data of the [target]'s type is stored under the relation's full [id] in archetypes.
 * The [type] points to another component this relation references.
 *
 * ```
 * [key]   bits: 0x00FFFFFF00000000
 * [value] bits: 0xFF000000FFFFFFFF
 * ```
 *
 * @property type The part of the relation which determines the data type of the full relation.
 * @property target The part of the relation that points to another entity.
 *
 */
@Serializable
@JvmInline
public value class Relation private constructor(
    public val id: GearyComponentId
) : Comparable<Relation> {
    public val type: GearyComponentId get() = id and RELATION_KEY_MASK shr 32
    public val target: GearyEntityId get() = id and RELATION_VALUE_MASK and RELATION.inv()

    override fun compareTo(other: Relation): Int = id.compareTo(other.id)

    override fun toString(): String = "${type.readableString()} to ${target.readableString()}"

    public companion object {
        public fun of(
            relation: GearyComponentId,
            target: GearyEntityId
        ): Relation = Relation(
            (relation shl 32 and RELATION_KEY_MASK)
                    or (target and RELATION_VALUE_MASK)
                    or RELATION
        )

        public fun of(key: KClass<*>, target: KClass<*>): Relation = GearyContextKoin {
            of(componentId(key), componentId(target))
        }

        public inline fun <reified Y : GearyComponent, reified T : GearyComponent> of(): Relation = GearyContextKoin {
            of(componentId<Y>(), componentId<T>())
        }

        public inline fun <reified Y : GearyComponent> of(target: GearyEntityId): Relation = GearyContextKoin {
            of(componentId<Y>(), target)
        }

        /**
         * Creates a relation from an id that is assumed to be valid. Use this to avoid boxing Relation because of
         * the nullable type on [toRelation].
         */
        public fun of(id: GearyComponentId): Relation = Relation(id)
    }
}

public fun GearyComponentId.toRelation(): Relation? = Relation.of(this).takeIf { isRelation() }
