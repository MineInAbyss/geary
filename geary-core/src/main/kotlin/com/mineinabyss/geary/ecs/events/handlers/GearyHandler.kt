package com.mineinabyss.geary.ecs.events.handlers

import com.mineinabyss.geary.ecs.accessors.*
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.systems.GearyListener
import com.mineinabyss.idofront.messaging.logError
import kotlin.reflect.KProperty

@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "Trying to use entity scope in event, use EventResultScope instead"
)
public annotation class WrongScope

/**
 * Placed within a [GearyListener] as an `object` or `inner class` to fire when an event matching
 * specified components gets fired on an entity matching the listener's components.
 */
public abstract class GearyHandler : AccessorHolder() {
    public lateinit var parentHolder: AccessorHolder
        internal set

    public abstract fun ResultScope.handle(event: EventResultScope)

    /** Be sure [event] is of the same type as this listener wants! */
    public open fun runEvent(event: GearyEntity, entityScope: RawAccessorDataScope, eventScope: RawAccessorDataScope) {
        try {
            iteratorFor(eventScope).forEach { eventData ->
                parentHolder.iteratorFor(entityScope).forEach { entityData ->
                    val entityResult = ResultScope(entityScope.entity, entityData)
                    val eventResult = EventResultScope(event, eventData)
                    preHandle(entityResult, eventResult)
                }
            }
        } catch (e: Exception) {
            logError("Failed to run event ${parentHolder::class.simpleName}")
            e.printStackTrace()
        }
    }

    internal open fun preHandle(entityResult: ResultScope, eventResult: EventResultScope) {
        entityResult.handle(eventResult)
    }

    @Suppress("UNCHECKED_CAST")
    @WrongScope
    public operator fun <T> Accessor<T>.getValue(thisRef: ResultScope, property: KProperty<*>): T =
        with(this@GearyHandler as AccessorHolder) { getValue(thisRef, property) }
}


