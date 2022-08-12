package com.mineinabyss.geary.helpers

import kotlin.reflect.typeOf
import com.mineinabyss.geary.datatypes.Component as GC
import com.mineinabyss.geary.datatypes.Entity as GE

/** Gets a component of type [T] from an entity, returning null if [T] is nullable, or an error otherwise. */
public inline fun <reified T : GC?> GE.nullOrError(): T {
    val data = get(componentId<T>()) as? T?
    if (!typeOf<T>().isMarkedNullable && data == null) {
        error("")
    }
    return data as T
}

/** Runs when an entity has all passed components present. */
public inline fun <R, reified T : GC?> GE.with(let: (T) -> R): R? =
    runCatching { let(nullOrError()) }.getOrNull()

/** Runs when an entity has all passed components present. */
public inline fun <R, reified T : GC?, reified T2 : GC?> GE.with(let: (T, T2) -> R): R? =
    runCatching { let(nullOrError(), nullOrError()) }.getOrNull()

/** Runs when an entity has all passed components present. */
public inline fun <reified T : GC, reified T2 : GC, reified T3 : GC> GE.with(let: (T, T2, T3) -> Unit): Unit? =
    runCatching { let(nullOrError(), nullOrError(), nullOrError()) }.getOrNull()

/** Runs when an entity has all passed components present. */
public inline fun <reified T : GC, reified T2 : GC, reified T3 : GC, reified T4 : GC> GE.with(let: (T, T2, T3, T4) -> Unit): Unit? =
    runCatching { let(nullOrError(), nullOrError(), nullOrError(), nullOrError()) }.getOrNull()

/** Runs when an entity has all passed components present. */
public inline fun <reified T : GC, reified T2 : GC, reified T3 : GC, reified T4 : GC, reified T5 : GC> GE.with(let: (T, T2, T3, T4, T5) -> Unit): Unit? =
    runCatching { let(nullOrError(), nullOrError(), nullOrError(), nullOrError(), nullOrError()) }.getOrNull()

/** Runs when an entity has all passed components present. */
public inline fun <reified T : GC, reified T2 : GC, reified T3 : GC, reified T4 : GC, reified T5 : GC, reified T6 : GC> GE.with(
    let: (T, T2, T3, T4, T5, T6) -> Unit
): Unit? =
    runCatching {
        let(
            nullOrError(),
            nullOrError(),
            nullOrError(),
            nullOrError(),
            nullOrError(),
            nullOrError()
        )
    }.getOrNull()

/** Runs when an entity has all passed components present. */
public inline fun <reified T : GC, reified T2 : GC, reified T3 : GC, reified T4 : GC, reified T5 : GC, reified T6 : GC, reified T7 : GC> GE.with(
    let: (T, T2, T3, T4, T5, T6, T7) -> Unit
): Unit? =
    runCatching {
        let(
            nullOrError(),
            nullOrError(),
            nullOrError(),
            nullOrError(),
            nullOrError(),
            nullOrError(),
            nullOrError()
        )
    }.getOrNull()
