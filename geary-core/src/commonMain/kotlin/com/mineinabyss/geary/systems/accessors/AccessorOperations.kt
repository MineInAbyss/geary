package com.mineinabyss.geary.systems.accessors

import com.mineinabyss.geary.datatypes.Component
import com.mineinabyss.geary.datatypes.HOLDS_DATA
import com.mineinabyss.geary.datatypes.withRole
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.componentIdWithNullable
import com.mineinabyss.geary.systems.accessors.type.*
import kotlin.properties.ReadOnlyProperty

/**
 * An empty interface that limits [AccessorBuilder] helper functions only to classes that use [Accessor]s.
 */
open class AccessorOperations {
    /** Accesses a component, ensuring it is on the entity. */
    inline fun <reified T : Any> get(): ComponentAccessor<T> {
        return ComponentAccessor(componentId<T>().withRole(HOLDS_DATA))
    }

    /**
     * Accesses a component, allows removing it by setting to null.
     * As a result, the type is nullable since it may be removed during system runtime.
     */
    inline fun <reified T> getRemovable(): RemovableComponentAccessor<T> {
        return RemovableComponentAccessor(componentId<T>().withRole(HOLDS_DATA))
    }

    /**
     * Accesses a component or provides a [default] if the entity doesn't have it.
     * Default gets recalculated on every call to the accessor.
     */
    inline fun <reified T> getOrDefault(noinline default: () -> T): ComponentOrDefaultAccessor<T> {
        return ComponentOrDefaultAccessor(componentId<T>(), default)
    }

    /** Maps an accessor, will recalculate on every call. */
    fun <T, U, A : ReadOnlyAccessor<T>> A.map(mapping: (T) -> U): ReadOnlyAccessor<U> {
        return ReadOnlyProperty { record, property ->
            val value = getValue(record, property)
            mapping(value)
        }
    }

    /** Accesses a component or `null` if the entity doesn't have it. */
    inline fun <reified T> getOrNull(): ComponentOrDefaultAccessor<T?> {
        return getOrDefault { null }
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
    inline fun <reified K : Component?, reified T : Component?> getRelations(): RelationsAccessor {
        return RelationsAccessor(componentIdWithNullable<K>(), componentIdWithNullable<T>())
    }

    inline fun <reified K : Component?, reified T : Component?> getRelationsWithData(): RelationsWithDataAccessor<K, T> {
        return RelationsWithDataAccessor(componentIdWithNullable<K>(), componentIdWithNullable<T>())
    }
}
