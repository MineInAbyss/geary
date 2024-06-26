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
import kotlin.reflect.typeOf

abstract class AccessorOperations {
    abstract val cacheAccessors: Boolean

    @PublishedApi
    internal val accessors: MutableSet<Accessor> = mutableSetOf()

    @PublishedApi
    internal val cachingAccessors: MutableSet<ComponentAccessor<*>> = mutableSetOf()

    /** Accesses a component, ensuring it is on the entity. */
    protected inline fun <reified T : Any> QueriedEntity.get(): ComponentAccessor<T> {
        return addAccessor {
            ComponentAccessor(null, componentId<T>().withRole(HOLDS_DATA))
        }
    }

    protected inline fun <reified T> QueriedEntity.getPotentiallyNullable(): ReadOnlyAccessor<T> {
        val t = typeOf<T>()
        return addAccessor {
            val id = componentId(t).withRole(HOLDS_DATA)
            val compAccessor = ComponentAccessor<T & Any>(null, id)
            if(t.isMarkedNullable)
                ComponentOrDefaultAccessor<T?>(compAccessor, id) { null }
            else compAccessor
        } as ReadOnlyAccessor<T>
    }

    /** Accesses a data stored in a relation with kind [K] and target type [T], ensuring it is on the entity. */
    protected inline fun <reified K : Any, reified T : Any> QueriedEntity.getRelation(): ComponentAccessor<T> {
        return addAccessor { ComponentAccessor(null, Relation.of<K, T>().id) }
    }

    inline fun <T : Accessor> addAccessor(create: () -> T): T {
        val accessor = create()
        accessors.add(accessor)
        if (accessor is ComponentAccessor<*>) cachingAccessors.add(accessor)
        if (accessor.originalAccessor != null) accessors.remove(accessor.originalAccessor)
        return accessor
    }

    /**
     * Accesses a component or provides a [default] if the entity doesn't have it.
     * Default gets recalculated on every call to the accessor.
     */
    fun <T> ComponentAccessor<T & Any>.orDefault(default: () -> T): ComponentOrDefaultAccessor<T> {
        return addAccessor { ComponentOrDefaultAccessor(this, id, default) }
    }

    /** Maps an accessor, will recalculate on every call. */
    fun <T, U, A : ReadOnlyAccessor<T>> A.map(mapping: (T) -> U): ReadOnlyAccessor<U> {
        return addAccessor {
            when (this) {
                is FamilyMatching -> object : ReadOnlyAccessor<U> by MappedAccessor(this, mapping),
                    FamilyMatching by this {}

                else -> MappedAccessor(this, mapping)
            }
        }
    }

    /** Accesses a component or `null` if the entity doesn't have it. */
    fun <T> ComponentAccessor<T & Any>.orNull(): ComponentOrDefaultAccessor<T?> {
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
    protected inline fun <reified K : Component?, reified T : Component?> QueriedEntity.getRelations(): RelationsAccessor {
        return addAccessor { RelationsAccessor(null, componentIdWithNullable<K>(), componentIdWithNullable<T>()) }
    }

    /** @see getRelations */
    protected inline fun <reified K : Component?, reified T : Component?> QueriedEntity.getRelationsWithData(): RelationsWithDataAccessor<K, T> {
        return addAccessor {
            RelationsWithDataAccessor(
                null,
                componentIdWithNullable<K>(),
                componentIdWithNullable<T>()
            )
        }
    }

    protected operator fun QueriedEntity.invoke(init: MutableFamily.Selector.And.() -> Unit) {
        val family = com.mineinabyss.geary.datatypes.family.family(init)
        extraFamilies.add(family)
    }
}
