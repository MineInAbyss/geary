package com.mineinabyss.geary.systems.accessors

import com.mineinabyss.geary.datatypes.GearyComponent
import com.mineinabyss.geary.datatypes.HOLDS_DATA
import com.mineinabyss.geary.datatypes.withRole
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.componentIdWithNullable
import com.mineinabyss.geary.systems.accessors.types.ComponentAccessor
import com.mineinabyss.geary.systems.accessors.types.ComponentOrDefaultAccessor
import com.mineinabyss.geary.systems.accessors.types.RelationWithDataAccessor

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
     * Queries for a specific relation or by kind/target.
     *
     * #### Nullability
     * Additional checks are done if [K] or [T] are not nullable:
     * - [K] is NOT nullable => the relation must hold data.
     * - [T] is NOT nullable => the relation target must also be present as a component with data on the entity.
     *
     * #### Query by kind/target
     * - One of [K] or [T] is [Any] => gets all relations matching the other (specified) type.
     * - Note: nullability rules are still upheld with [Any].
     */
    public inline fun <reified K : GearyComponent?, reified T : GearyComponent?> relation(): AccessorBuilder<RelationWithDataAccessor<K, T>> {
        return AccessorBuilder { holder, index ->
            holder._family.hasRelation<K, T>()
            RelationWithDataAccessor(index, componentIdWithNullable<K>(), componentIdWithNullable<T>())
        }
    }
}
