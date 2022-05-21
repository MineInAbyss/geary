package com.mineinabyss.geary.systems.accessors

import com.mineinabyss.geary.datatypes.GearyComponent
import com.mineinabyss.geary.datatypes.HOLDS_DATA
import com.mineinabyss.geary.datatypes.withRole
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.systems.accessors.types.ComponentAccessor
import com.mineinabyss.geary.systems.accessors.types.ComponentOrDefaultAccessor
import com.mineinabyss.geary.systems.accessors.types.RelationWithDataAccessor
import kotlin.reflect.typeOf

/**
 * An empty interface that limits [AccessorBuilder] helper functions only to classes that use [Accessor]s.
 */
public open class AccessorOperations {
    /** Gets a component, ensuring it is on the entity. */
    public inline fun <reified T : GearyComponent> get(): AccessorBuilder<ComponentAccessor<T>> {
        return AccessorBuilder { holder, index ->
            val component = componentId<T>().withRole(HOLDS_DATA)
            holder._family.has(component)
            ComponentAccessor(index, component)
        }
    }

    /** Gets a component or provides a [default] if the entity doesn't have it. */
    public inline fun <reified T : GearyComponent?> getOrDefault(
        default: T
    ): AccessorBuilder<ComponentOrDefaultAccessor<T>> {
        return AccessorBuilder { _, index ->
            val component = componentId<T>().withRole(HOLDS_DATA)
            ComponentOrDefaultAccessor(index, component, default)
        }
    }

    /** Gets a component or `null` if the entity doesn't have it. */
    public inline fun <reified T : GearyComponent?> getOrNull(): AccessorBuilder<ComponentOrDefaultAccessor<T?>> {
        return getOrDefault(null)
    }

    /**
     * This function allows you to access a specific relation or all relations with a certain key or value.
     *
     * #### Nullability
     * If [K] is nullable, just the relation needs to be present.
     *
     * If [K] is NOT nullable, the entity must have a component of type [K] set.
     *
     * - `relation<String?, Int>()` will match the specific relation with key [String] and value [Int], regardless
     * of whether the entity has a component [Int] with data.
     *
     * #### Matching any relation
     * If one type is [Any], this will get all relations matching the other type.
     *
     * - `relation<String?, Any>()` will match against all relations with a [String] key and any value.
     * - `relation<Any, String>()` will match against all relations with any key and a [String] value,
     *   so long as the entity also has a key of that type set.
     *
     * @see flatten
     */
    public inline fun <reified K : GearyComponent?, reified T : GearyComponent?> relation(): AccessorBuilder<RelationWithDataAccessor<K, T>> {
        return AccessorBuilder { holder, index ->
            val kind = typeOf<K>()
            val target = typeOf<T>()
            val kindIsNullable = kind.isMarkedNullable
            val relationKind = if (kind.classifier == Any::class) null else componentId(kind)
            val relationTarget = if (target.classifier == Any::class) null else componentId(target)
            //TODO could we reuse code between hasRelation and here?
            holder._family.hasRelation(kind, target)
            RelationWithDataAccessor(index, kindIsNullable, relationKind, relationTarget)
        }
    }
}
