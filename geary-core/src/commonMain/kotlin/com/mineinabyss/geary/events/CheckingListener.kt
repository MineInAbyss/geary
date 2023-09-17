package com.mineinabyss.geary.events

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.components.RequestCheck
import com.mineinabyss.geary.components.events.FailedCheck
import com.mineinabyss.geary.systems.Listener
import com.mineinabyss.geary.systems.accessors.Pointers

/**
 * A listener that runs a check on matched events, adding [FailedCheck] to the event when the check fails.
 */
abstract class CheckingListener() : Listener() {
    init {
        event.mutableFamily.has<RequestCheck>()
    }

    abstract fun Pointers.check(): Boolean

    @OptIn(UnsafeAccessors::class)
    override fun Pointers.handle() {
        if (!check()) event.entity.apply {
            remove<RequestCheck>()
            add<FailedCheck>()
        }
    }
}
