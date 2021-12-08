package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.accessors.AccessorHolder
import com.mineinabyss.geary.ecs.accessors.EventResultScope
import com.mineinabyss.geary.ecs.accessors.RawAccessorDataScope
import com.mineinabyss.geary.ecs.accessors.ResultScope
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.events.ComponentAddEvent
import com.mineinabyss.idofront.messaging.logError

public abstract class GearyEventHandler(
) : AccessorHolder() {
    public lateinit var parentHolder: AccessorHolder
        internal set

    public abstract fun ResultScope.handle(event: EventResultScope)

    /** Be sure [event] is of the same type as this listener wants! */
    public open fun runEvent(event: GearyEntity, entityScope: RawAccessorDataScope, eventScope: RawAccessorDataScope) {
        try {
            iteratorFor(eventScope).forEach { eventData ->
                iteratorFor(entityScope).forEach { entityData ->
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
        println("Handling")
        entityResult.handle(eventResult)
    }
}


public abstract class ComponentAddHandler: GearyEventHandler() {
    private val EventResultScope.component by get<ComponentAddEvent>().map { it.component }
    private val checkedComponents by lazy { parentHolder.family.components }
    override fun preHandle(entityResult: ResultScope, eventResult: EventResultScope) {
        println("Running prehandle")
        if(eventResult.component in checkedComponents) {
            println("Component was in!")
            super.preHandle(entityResult, eventResult)
        }
    }
}
