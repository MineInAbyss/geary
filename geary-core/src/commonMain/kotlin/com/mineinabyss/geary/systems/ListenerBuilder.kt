package com.mineinabyss.geary.systems

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.components.RequestCheck
import com.mineinabyss.geary.components.events.FailedCheck
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.systems.query.EventQuery

fun <T : EventQuery> GearyModule.listener(
    query: T,
    handle: T.() -> Unit
): Listener<*> {
//    query.buildFamily()
    val listener = Listener(
        query,
        query.buildFamilies(),
        handle,
    )
    return pipeline.addListener(listener)
}

@OptIn(UnsafeAccessors::class)
fun <T : EventQuery> GearyModule.checkingListener(
    query: T,
    check: T.() -> Boolean
): Listener<*> {
    val families = query.buildFamilies()
    val listener = Listener(
        query,
        families.copy(event = family {
            has<RequestCheck>()
            add(families.event)
        }),
    ) {
        if (!check()) event.entity.apply {
            remove<RequestCheck>()
            add<FailedCheck>()
        }
    }
    return pipeline.addListener(listener)
}

/**
 * A listener that runs a check on matched events, adding [FailedCheck] to the event when the check fails.
 */
//TODO reimplement
//abstract class CheckingListener() : Listener() {
//    init {
//        event.mutableFamily.has<RequestCheck>()
//    }
//
//    abstract fun Pointers.check(): Boolean
//
//    @OptIn(UnsafeAccessors::class)
//    override fun Pointers.handle() {
//    }
//}
