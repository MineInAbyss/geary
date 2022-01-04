package com.mineinabyss.geary.ecs.events.handlers

import com.mineinabyss.geary.ecs.accessors.EventScope
import com.mineinabyss.geary.ecs.accessors.ResultScope
import com.mineinabyss.geary.ecs.events.AddedComponent

/**
 * A handler which runs when all the listener's requested entity components are added.
 *
 * The event itself will always contain a single [AddedComponent] component.
 */
public abstract class ComponentAddHandler : GearyHandler() {
    private val EventScope.component by get<AddedComponent>().map { it.component }
    private val checkedComponents by lazy { parentHolder.family.components }

    override fun preHandle(entityResult: ResultScope, eventResult: EventScope) {
        if (eventResult.component in checkedComponents) {
            super.preHandle(entityResult, eventResult)
        }
    }
}
