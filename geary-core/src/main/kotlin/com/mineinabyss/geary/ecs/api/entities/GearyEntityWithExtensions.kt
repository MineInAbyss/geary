package com.mineinabyss.geary.ecs.api.entities

import com.mineinabyss.geary.ecs.api.engine.componentId
import kotlin.reflect.typeOf
import com.mineinabyss.geary.ecs.api.GearyComponent as GC
import com.mineinabyss.geary.ecs.api.entities.GearyEntity as GE

public inline fun <reified T : GC?> GE.nullOrError(): T {
    val data = get(componentId<T>()) as? T?
    if (!typeOf<T>().isMarkedNullable && data == null) {
        error("")
    }
    return data as T
}

/** Runs a block when an entity has all passed components present. */
public inline fun <R, reified T : GC?> GE.with(let: (T) -> R): R? =
    runCatching { let(nullOrError()) }.getOrNull()

public inline fun <R, reified T : GC?, reified T2 : GC?> GE.with(let: (T, T2) -> R): R? =
    runCatching { let(nullOrError(), nullOrError()) }.getOrNull()

public inline fun <reified T : GC, reified T2 : GC, reified T3 : GC> GE.with(let: (T, T2, T3) -> Unit): Unit? {
    return let(get() ?: return null, get() ?: return null, get() ?: return null)
}

public inline fun <reified T : GC, reified T2 : GC, reified T3 : GC, reified T4 : GC> GE.with(let: (T, T2, T3, T4) -> Unit): Unit? {
    return let(get() ?: return null, get() ?: return null, get() ?: return null, get() ?: return null)
}

public inline fun <reified T : GC, reified T2 : GC, reified T3 : GC, reified T4 : GC, reified T5 : GC> GE.with(let: (T, T2, T3, T4, T5) -> Unit): Unit? {
    return let(
        get() ?: return null,
        get() ?: return null,
        get() ?: return null,
        get() ?: return null,
        get() ?: return null
    )
}

// NULLABLES

/** Runs a block, reading all passed components or null if not present. */
public inline fun <reified T : GC> GE.withNullable(let: (T?) -> Unit) {
    return let(get())
}

public inline fun <reified T : GC, reified T2 : GC> GE.withNullable(let: (T?, T2?) -> Unit) {
    return let(get(), get())
}

public inline fun <reified T : GC, reified T2 : GC, reified T3 : GC> GE.withNullable(let: (T?, T2?, T3?) -> Unit) {
    return let(get(), get(), get())
}

public inline fun <reified T : GC, reified T2 : GC, reified T3 : GC, reified T4 : GC> GE.withNullable(let: (T?, T2?, T3?, T4?) -> Unit) {
    return let(get(), get(), get(), get())
}

public inline fun <reified T : GC, reified T2 : GC, reified T3 : GC, reified T4 : GC, reified T5 : GC> GE.withNullable(
    let: (T?, T2?, T3?, T4?, T5?) -> Unit
) {
    return let(get(), get(), get(), get(), get())
}
