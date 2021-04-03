package com.mineinabyss.geary.ecs.actions.components

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.actions.GearyAction
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Adds components to the entity this action is run on.
 */
@Serializable
@SerialName("add")
public class AddComponentAction(
    private val components: Set<@Polymorphic GearyComponent>
) : GearyAction() {
    override fun runOn(entity: GearyEntity): Boolean {
        entity.setAll(components)
        return true
    }
}

@Serializable
@SerialName("remove")
public data class RemoveComponentAction(override val components: Set<String>) : ComponentAction() {
    override fun runOn(entity: GearyEntity): Boolean {
        componentClasses.forEach {
            Engine.removeComponentFor(entity.id, componentId(it))
        }
        return true
    }
}

@Serializable
@SerialName("disable")
public class DisableComponentAction(override val components: Set<String>) : ComponentAction() {
    override fun runOn(entity: GearyEntity): Boolean {
        componentClasses.forEach { entity.remove(it) }
        return true
    }
}

@Serializable
@SerialName("enable")
public data class EnableComponentAction(override val components: Set<String>) : ComponentAction() {
    override fun runOn(entity: GearyEntity): Boolean {
        componentClasses.forEach { entity.remove(it) }
        return true
    }
}
