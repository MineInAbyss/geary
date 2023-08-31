package com.mineinabyss.geary.systems.accessors

import com.mineinabyss.geary.datatypes.HOLDS_DATA
import com.mineinabyss.geary.datatypes.Record
import com.mineinabyss.geary.datatypes.withRole
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.systems.accessors.type.ComponentAccessor
import com.mineinabyss.geary.systems.accessors.type.ComponentOrDefaultAccessor
import kotlin.properties.ReadOnlyProperty

/**
 * An empty interface that limits [AccessorBuilder] helper functions only to classes that use [Accessor]s.
 */
open class AccessorOperations {

    inline fun <reified T> get(): ComponentAccessor<T> {
        return ComponentAccessor(componentId<T>().withRole(HOLDS_DATA))
    }

    inline fun <reified T> getOrDefault(noinline default: () -> T): ComponentOrDefaultAccessor<T> {
        return ComponentOrDefaultAccessor(componentId<T>(), default)
    }

    fun <T, U, A: ReadOnlyProperty<Record, T>> A.map(mapping: (T) -> U): ReadOnlyProperty<Record, U> {
        return ReadOnlyProperty { record, property ->
            val value = getValue(record, property)
            mapping(value)
        }
    }

//    /** Gets a component, ensuring it is on the entity. */
//    inline fun <reified T : Component> get(): AccessorBuilder<ComponentAccessor<T>> {
//        return AccessorBuilder { holder, index ->
//            val component = componentId<T>().withRole(HOLDS_DATA)
//            holder.mutableFamily.has(component)
//            ComponentAccessor(index, component)
//        }
//    }
//
//    /** Gets a component or provides a [default] if the entity doesn't have it. */
//    inline fun <reified T : Component?> getOrDefault(
//        default: T
//    ): AccessorBuilder<ComponentOrDefaultAccessor<T>> {
//        return AccessorBuilder { _, index ->
//            val component = componentId<T>().withRole(HOLDS_DATA)
//            ComponentOrDefaultAccessor(index, component, default)
//        }
//    }
//
//    /** Gets a component or `null` if the entity doesn't have it. */
//    inline fun <reified T : Component?> getOrNull(): AccessorBuilder<ComponentOrDefaultAccessor<T?>> {
//        return getOrDefault(null)
//    }
//
//    /**
//     * Queries for a specific relation or by kind/target.
//     *
//     * #### Nullability
//     * Additional checks are done if [K] or [T] are not nullable:
//     * - [K] is NOT nullable => the relation must hold data.
//     * - [T] is NOT nullable => the relation target must also be present as a component with data on the entity.
//     *
//     * #### Query by kind/target
//     * - One of [K] or [T] is [Any] => gets all relations matching the other (specified) type.
//     * - Note: nullability rules are still upheld with [Any].
//     */
//    inline fun <reified K : Component?, reified T : Component?> getRelations(): AccessorBuilder<RelationWithDataAccessor<K, T>> {
//        return AccessorBuilder { holder, index ->
//            holder.mutableFamily.hasRelation<K, T>()
//            RelationWithDataAccessor(index, componentIdWithNullable<K>(), componentIdWithNullable<T>())
//        }
//    }
}
