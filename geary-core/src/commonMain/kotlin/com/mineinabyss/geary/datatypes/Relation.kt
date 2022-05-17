package com.mineinabyss.geary.datatypes

import com.mineinabyss.geary.helpers.componentId
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.reflect.KClass

/**
 * A combination of a data [kind] and [target] entity that represents a relation between two entities.
 *
 * When a relation is added to a `source` entity,
 * we say the `source` has a relation of the kind [kind] with [target].
 *
 * For example: `Alice` has a relation of the kind `Friend` with `Bob`.
 *
 * @property kind The part of the relation which determines its data type.
 * @property target The part of the relation that points to another entity.
 *
 */
@Serializable
@JvmInline
public value class Relation private constructor(
    public val id: GearyComponentId
) : Comparable<Relation> {
    /*
    * Internal representation of bits:
    * [kind]   0xFFFFFFFF00000000
    * [target] 0x00000000FFFFFFFF
    */
    public val kind: GearyComponentId get() = id and RELATION_KIND_MASK shr 32
    public val target: GearyEntityId get() = id and RELATION_TARGET_MASK and RELATION.inv()

    override fun compareTo(other: Relation): Int = id.compareTo(other.id)

    override fun toString(): String = "${kind.readableString()} to ${target.readableString()}"

    public companion object {
        public fun of(
            kind: GearyComponentId,
            target: GearyEntityId
        ): Relation = Relation(
            (kind shl 32 and RELATION_KIND_MASK)
                    or (target and RELATION_TARGET_MASK)
                    or RELATION
        )

        public fun of(kind: KClass<*>, target: KClass<*>): Relation =
            of(componentId(kind), componentId(target))

        public inline fun <reified K : GearyComponent, reified T : GearyComponent> of(): Relation =
            of(componentId<K>(), componentId<T>())

        public inline fun <reified K : GearyComponent> of(target: GearyEntity): Relation =
            of(componentId<K>(), target.id)

        /**
         * Creates a relation from an id that is assumed to be valid. Use this to avoid boxing Relation because of
         * the nullable type on [toRelation].
         */
        public fun of(id: GearyComponentId): Relation = Relation(id)

    }
}

public fun GearyComponentId.toRelation(): Relation? = Relation.of(this).takeIf { isRelation() }
