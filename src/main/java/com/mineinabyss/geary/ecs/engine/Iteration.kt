package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.geary
import com.mineinabyss.geary.ecs.GearyComponent as GC

//TODO support component families with infix functions
public inline fun Engine.forEach(
    vararg components: ComponentClass,
    andNot: Array<out ComponentClass> = emptyArray(),
    run: GearyEntity.(List<GC>) -> Unit
) {
    getBitsMatching(*components, andNot = andNot).forEachBit { index ->
        geary(index).run(components.map { getComponentFor(it, index) ?: return@forEachBit })
    }
}

public inline fun <reified T : GC> Engine.forEach(
    vararg andNot: ComponentClass = emptyArray(),
    run: GearyEntity.(T) -> Unit
) {
    forEach(T::class, andNot = andNot) {
        run(it[0] as T)
    }
}

public inline fun <reified T : GC, reified T2 : GC> Engine.forEach(
    vararg andNot: ComponentClass = emptyArray(),
    run: GearyEntity.(T, T2) -> Unit
) {
    forEach(T::class, T2::class, andNot = andNot) {
        run(it[0] as T, it[1] as T2)
    }
}

public inline fun <reified T : GC, reified T2 : GC, reified T3 : GC> Engine.forEach(
    vararg andNot: ComponentClass = emptyArray(),
    run: GearyEntity.(T, T2, T3) -> Unit
) {
    forEach(T::class, T2::class, T3::class, andNot = andNot) {
        run(it[0] as T, it[1] as T2, it[2] as T3)
    }
}

public inline fun <reified T : GC, reified T2 : GC, reified T3 : GC, reified T4 : GC> Engine.forEach(
    vararg andNot: ComponentClass = emptyArray(),
    run: GearyEntity.(T, T2, T3, T4) -> Unit
) {
    forEach(T::class, T2::class, T3::class, andNot = andNot) {
        run(it[0] as T, it[1] as T2, it[2] as T3, it[3] as T4)
    }
}

public inline fun <reified T : GC, reified T2 : GC, reified T3 : GC, reified T4 : GC, reified T5 : GC> Engine.forEach(
    vararg andNot: ComponentClass = emptyArray(),
    run: GearyEntity.(T, T2, T3, T4, T5) -> Unit
) {
    forEach(T::class, T2::class, T3::class, andNot = andNot) {
        run(it[0] as T, it[1] as T2, it[2] as T3, it[3] as T4, it[4] as T5)
    }
}

public inline fun <reified T : GC, reified T2 : GC, reified T3 : GC, reified T4 : GC, reified T5 : GC, reified T6 : GC> Engine.forEach(
    vararg andNot: ComponentClass = emptyArray(),
    run: GearyEntity.(T, T2, T3, T4, T5, T6) -> Unit
) {
    forEach(T::class, T2::class, T3::class, andNot = andNot) {
        run(it[0] as T, it[1] as T2, it[2] as T3, it[3] as T4, it[4] as T5, it[5] as T6)
    }
}

public inline fun <reified T : GC, reified T2 : GC, reified T3 : GC, reified T4 : GC, reified T5 : GC, reified T6 : GC, reified T7 : GC> Engine.forEach(
    vararg andNot: ComponentClass = emptyArray(),
    run: GearyEntity.(T, T2, T3, T4, T5, T6, T7) -> Unit
) {
    forEach(T::class, T2::class, T3::class, andNot = andNot) {
        run(it[0] as T, it[1] as T2, it[2] as T3, it[3] as T4, it[4] as T5, it[5] as T6, it[5] as T7)
    }
}
