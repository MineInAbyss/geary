package com.mineinabyss.geary.datatypes

import com.mineinabyss.geary.helpers.readableString
import kotlin.jvm.JvmInline

/**
 * A combination of a data [kind] and [target] entity that represents a relation between two entities.
 *
 * When a relation is added to a `source` entity,
 * we say the `source` has a relation of the kind [kind] with [target].
 *
 * For example: `Alice` has a relation of the kind `Friend` with `Bob`.
 *
 * @property kind The part of the relation which determines its data type. It includes all the relation's type roles,
 * except the [RELATION] role.
 * @property target The part of the relation that points to another entity.
 */
@JvmInline
value class Relation private constructor(
    val id: ComponentId
) : Comparable<Relation> {
    /*
    * Internal representation of bits:
    * [kind]   0xFFFFFFFF00000000
    * [target] 0x00000000FFFFFFFF
    */
    val kind: ComponentId
        get() = id and TYPE_ROLES_MASK.inv() and RELATION_KIND_MASK shr 32 or (id and TYPE_ROLES_MASK).withoutRole(RELATION)
    val target: EntityId get() = id and RELATION_TARGET_MASK

    override fun compareTo(other: Relation): Int = id.compareTo(other.id)

    override fun toString(): String = "${kind.readableString(null)} to ${target.readableString(null)}"

    companion object {
        fun of(
            kind: ComponentId, target: EntityId
        ): Relation = Relation(
            (kind shl 32 and RELATION_KIND_MASK and TYPE_ROLES_MASK.inv()) // Add kind entity id shifted left
                    or (kind and TYPE_ROLES_MASK) // Add type roles on kind
                    or RELATION // Add relation type role
                    or (target and RELATION_TARGET_MASK) // Add target, stripping any type roles
        )

        /**
         * Creates a relation from an id that is assumed to be valid. Use this to avoid boxing Relation because of
         * the nullable type on [toRelation].
         */
        fun of(id: ComponentId): Relation = Relation(id)
    }
}

fun ComponentId.toRelation(): Relation? = Relation.of(this).takeIf { isRelation() }
