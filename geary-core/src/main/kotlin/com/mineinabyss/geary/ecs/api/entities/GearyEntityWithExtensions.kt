package com.mineinabyss.geary.ecs.api.entities

import com.mineinabyss.geary.ecs.api.GearyComponent as GC
import com.mineinabyss.geary.ecs.api.entities.GearyEntity as GE

/** Runs a block when an entity has all passed components present. */
public inline fun <reified T : GC> GE.with(let: (T) -> Unit): Unit? {
    return let(get() ?: return null)
}

public inline fun <reified T : GC, reified T2: GC> GE.with(let: (T, T2) -> Unit): Unit? {
    return let(get() ?: return null, get() ?: return null)
}

public inline fun <reified T : GC, reified T2: GC, reified T3: GC> GE.with(let: (T, T2, T3) -> Unit): Unit? {
    return let(get() ?: return null, get() ?: return null, get() ?: return null)
}

public inline fun <reified T : GC, reified T2: GC, reified T3: GC, reified T4: GC> GE.with(let: (T, T2, T3, T4) -> Unit): Unit? {
    return let(get() ?: return null, get() ?: return null, get() ?: return null, get() ?: return null)
}

public inline fun <reified T : GC, reified T2: GC, reified T3: GC, reified T4: GC, reified T5: GC> GE.with(let: (T, T2, T3, T4, T5) -> Unit): Unit? {
    return let(get() ?: return null, get() ?: return null, get() ?: return null, get() ?: return null, get() ?: return null)
}

// NULLABLES

/** Runs a block, reading all passed components or null if not present. */
public inline fun <reified T : GC> GE.withNullable(let: (T?) -> Unit) {
    return let(get())
}

public inline fun <reified T : GC, reified T2: GC> GE.withNullable(let: (T?, T2?) -> Unit) {
    return let(get(), get())
}

public inline fun <reified T : GC, reified T2: GC, reified T3: GC> GE.withNullable(let: (T?, T2?, T3?) -> Unit) {
    return let(get(), get(), get())
}

public inline fun <reified T : GC, reified T2: GC, reified T3: GC, reified T4: GC> GE.withNullable(let: (T?, T2?, T3?, T4?) -> Unit) {
    return let(get(), get(), get(), get())
}

public inline fun <reified T : GC, reified T2: GC, reified T3: GC, reified T4: GC, reified T5: GC> GE.withNullable(let: (T?, T2?, T3?, T4?, T5?) -> Unit) {
    return let(get(), get(), get(), get(), get())
}
