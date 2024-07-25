package com.mineinabyss.geary.actions.event_binds

import com.mineinabyss.geary.actions.ActionGroup
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.actions.EmitEventAction
import com.mineinabyss.geary.datatypes.EntityType
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.observers.entity.observe
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.systems.builders.observe
import com.mineinabyss.geary.systems.query.query

fun GearyModule.bindEntityObservers() = observe<OnSet>()
    .involving(query<EntityObservers>())
    .exec { (observers) ->
        observers.observers.forEach { observer ->
            val actionGroup = ActionGroup(
                actions = observer.emit.flatten().map { EmitEventAction.wrapIfNotAction(it) }
            )
            entity.observe(observer.event.id).involving(EntityType(observer.involving.map { it.id })).exec {
                val context = ActionGroupContext(entity)
                actionGroup.execute(context)
            }
        }
        entity.remove<EntityObservers>()
    }

