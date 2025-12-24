package com.mineinabyss.geary.systems.accessors

import com.mineinabyss.geary.datatypes.HOLDS_DATA
import com.mineinabyss.geary.datatypes.withRole
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.modules.WorldScoped
import com.mineinabyss.geary.systems.accessors.type.ComponentAccessor
import com.mineinabyss.geary.systems.accessors.type.ComponentOrDefaultAccessor
import kotlin.reflect.typeOf

object Accessors {
    context(world: WorldScoped)
    inline fun <reified T> getPotentiallyNullable(): ReadWriteAccessor<T> {
        val t = typeOf<T>()
        val id = world.componentId(t).withRole(HOLDS_DATA)
        val compAccessor = ComponentAccessor<T & Any>(id)
        return (if (t.isMarkedNullable) ComponentOrDefaultAccessor<T?>(id) { null }
        else compAccessor) as ReadWriteAccessor<T>
    }
}

/** Accesses a data stored in a relation with kind [K] and target type [T], ensuring it is on the entity. */
//    protected inline fun <reified K : Any, reified T : Any> QueriedEntity.getRelation(): ComponentAccessor<T> {
//        return addAccessor { ComponentAccessor(world.componentProvider, null, world.relationOf<K, T>().id) }
//    }

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
//TODO reimplement
//    protected inline fun <reified K : Component?, reified T : Component?> QueriedEntity.getRelations(): RelationsAccessor {
//        return addAccessor { RelationsAccessor(world.componentProvider, null, world.componentIdWithNullable<K>(), world.componentIdWithNullable<T>()) }
//    }
//
//    /** @see getRelations */
//    protected inline fun <reified K : Component?, reified T : Component?> QueriedEntity.getRelationsWithData(): RelationsWithDataAccessor<K, T> {
//        return addAccessor {
//            RelationsWithDataAccessor(
//                world.componentProvider,
//                null,
//                world.componentIdWithNullable<K>(),
//                world.componentIdWithNullable<T>()
//            )
//        }
//    }
