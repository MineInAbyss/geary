package com.mineinabyss.geary.ecs.api.relations

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.engine.*
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

/**
 * A combination of two [GearyComponentId]s into one that represents a relation between
 * the two. Used for "adding a component to another component."
 *
 * Data of the [data]'s type is stored under the relation's full [id] in archetypes.
 * The [key] points to another component this relation references.
 *
 * ```
 * Parent bits:     0x00FFFFFF00000000
 * Component bits:  0xFF000000FFFFFFFF
 * ```
 *
 * @property data The part of the relation which determines the data type of the full relation.
 * @property key The part of the relation that points to another component on the entity.
 *
 */
@Serializable
@JvmInline
public value class Relation internal constructor(
    public val id: GearyComponentId
) : Comparable<Relation> {
    public val data: RelationDataType get() = RelationDataType(id and RELATION_PARENT_MASK shr 32)
    public val key: GearyComponentId get() = id and RELATION_COMPONENT_MASK and RELATION.inv()

    override fun compareTo(other: Relation): Int = id.compareTo(other.id)

    override fun toString(): String =
        id.toString(2)

    public companion object {
        public fun of(
            parent: RelationDataType,
            component: GearyComponentId
        ): Relation = Relation(
            (parent.id shl 32 and RELATION_PARENT_MASK)
                    or (component and RELATION_COMPONENT_MASK)
                    or RELATION
        )

        public fun of(parent: GearyComponentId, component: GearyComponentId = 0uL): Relation =
            of(RelationDataType(parent), component)

        public fun of(parent: KClass<*>, component: KClass<*>): Relation =
            of(componentId(parent), componentId(component))

        public inline fun <reified P : GearyComponent, reified C : GearyComponent> of(): Relation =
            of(componentId<P>(), componentId<C>())
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
public value class RelationDataType(public val id: GearyComponentId)

public fun GearyComponentId.toRelation(): Relation? =
    Relation(this).takeIf { isRelation() }
