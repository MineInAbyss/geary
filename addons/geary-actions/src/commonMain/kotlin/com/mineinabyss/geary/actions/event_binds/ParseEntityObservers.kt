package com.mineinabyss.geary.actions.event_binds

import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.execute
import com.mineinabyss.geary.datatypes.EntityType
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.observe
import com.mineinabyss.geary.observers.entity.observe
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.systems.query.query

fun Geary.bindEntityObservers() = observe<OnSet>()
    .involving(query<EntityObservers>())
    .exec { (observers) ->
        observers.observers.forEach { observer ->
            val actionGroup = observer.actionGroup
            entity.observe(observer.event).involving(EntityType(observer.involving)).exec {
                val context = ActionGroupContext(entity)
                actionGroup.execute(context)
            }
        }
        entity.remove<EntityObservers>()
    }

