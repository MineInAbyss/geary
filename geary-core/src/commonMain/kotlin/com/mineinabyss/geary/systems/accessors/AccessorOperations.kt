package com.mineinabyss.geary.systems.accessors

import com.mineinabyss.geary.datatypes.Component
import com.mineinabyss.geary.datatypes.HOLDS_DATA
import com.mineinabyss.geary.datatypes.Relation
import com.mineinabyss.geary.datatypes.family.MutableFamily
import com.mineinabyss.geary.datatypes.withRole
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.componentIdWithNullable
import com.mineinabyss.geary.systems.accessors.type.*
import com.mineinabyss.geary.systems.query.QueriedEntity
import kotlin.reflect.KProperty

open class AccessorOperations {
    /** Accesses a component, ensuring it is on the entity. */
    inline fun <reified T : Any> QueriedEntity.get(): ComponentAccessor<T> {
        return NonNullComponentAccessor(this, componentId<T>().withRole(HOLDS_DATA))
    }

    /** Accesses a data stored in a relation with kind [K] and target type [T], ensuring it is on the entity. */
    inline fun <reified K: Any, reified T : Any> QueriedEntity.getRelation(): ComponentAccessor<T> {
        return NonNullComponentAccessor(this, Relation.of<K, T>().id)
    }

    /**
     * Accesses a component, allows removing it by setting to null.
     * As a result, the type is nullable since it may be removed during system runtime.
     */
    fun <T: Any> ComponentAccessor<T>.removable(): RemovableComponentAccessor<T> {
        return RemovableComponentAccessor(queriedEntity, id)
    }

    /**
     * Accesses a component or provides a [default] if the entity doesn't have it.
     * Default gets recalculated on every call to the accessor.
     */
    fun <T> ComponentAccessor<T & Any>.orDefault(default: () -> T): ComponentOrDefaultAccessor<T> {
        return ComponentOrDefaultAccessor(id, default)
    }

    /** Maps an accessor, will recalculate on every call. */
    fun <T, U, A : ReadOnlyAccessor<T>> A.map(mapping: (T) -> U): ReadOnlyAccessor<U> {
        return object : ReadOnlyAccessor<U>, FamilyMatching {
            override val queriedEntity: QueriedEntity = TODO()
            override val family = (this@map as? FamilyMatching)?.family

            override fun getValue(thisRef: AccessorOperations, property: KProperty<*>): U {
                val value = this@map.getValue(thisRef, property)
                return mapping(value)
            }
        }
    }

    /** Accesses a component or `null` if the entity doesn't have it. */
    fun <T: Any> ComponentAccessor<T>.orNull(): ComponentOrDefaultAccessor<T?> {
        return orDefault { null }
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
    inline fun <reified K : Component?, reified T : Component?> QueriedEntity.getRelations(): RelationsAccessor {
        return RelationsAccessor(this, componentIdWithNullable<K>(), componentIdWithNullable<T>())
    }

    /** @see getRelations */
    inline fun <reified K : Component?, reified T : Component?> QueriedEntity.getRelationsWithData(): RelationsWithDataAccessor<K, T> {
        return RelationsWithDataAccessor(this, componentIdWithNullable<K>(), componentIdWithNullable<T>())
    }

    fun QueriedEntity.match(init: MutableFamily.Selector.And.() -> Unit) {
        val family = com.mineinabyss.geary.datatypes.family.family(init)
        extraFamilies.add(family)
    }
}
