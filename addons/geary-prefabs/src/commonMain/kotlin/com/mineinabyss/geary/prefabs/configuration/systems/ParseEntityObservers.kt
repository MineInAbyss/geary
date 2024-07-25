package com.mineinabyss.geary.prefabs.configuration.systems

import com.mineinabyss.geary.datatypes.EntityType
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.observers.entity.observe
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.prefabs.configuration.components.Action
import com.mineinabyss.geary.prefabs.configuration.components.EntityObservers
import com.mineinabyss.geary.prefabs.configuration.components.RoleCancelledException
import com.mineinabyss.geary.prefabs.configuration.components.RoleContext
import com.mineinabyss.geary.systems.builders.observe
import com.mineinabyss.geary.systems.query.query

fun GearyModule.bindEntityObservers() = observe<OnSet>()
    .involving(query<EntityObservers>())
    .exec { (observers) ->
        observers.observers.forEach { observer ->
            entity.observe(observer.event.id).involving(EntityType(observer.involving.map { it.id })).exec {
                val context = RoleContext(entity)
                try {
                    observer.emitEvents.forEach { event ->
                        when (val data = event.data) {
                            is Action -> with(data) { context.execute() }
                            else -> context.entity.emit(event = event.componentId, data = event.data)
                        }
                    }
                } catch (_: RoleCancelledException) {

                }
            }
        }
        entity.remove<EntityObservers>()
    }

