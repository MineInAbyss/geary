package com.mineinabyss.geary.ecs.actions.components

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.actions.GearyAction
import com.mineinabyss.geary.ecs.components.addComponents
import com.mineinabyss.geary.ecs.engine.Engine
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Adds components to the entity this action is run on.
 */
@Serializable
@SerialName("add")
public class AddComponentAction(
        private val components: Set<@Contextual GearyComponent>
) : GearyAction() {
    override fun runOn(entity: GearyEntity): Boolean {
        entity.addComponents(components)
        return true
    }
}

@Serializable
@SerialName("remove")
public data class RemoveComponentAction(override val components: Set<String>) : ComponentAction() {
    override fun runOn(entity: GearyEntity): Boolean {
        componentClasses.forEach {
            Engine.removeComponentFor(it, entity.gearyId)
        }
        return true
    }
}

@Serializable
@SerialName("disable")
public class DisableComponentAction(override val components: Set<String>) : ComponentAction() {
    override fun runOn(entity: GearyEntity): Boolean {
        componentClasses.forEach {
            Engine.disableComponentFor(it, entity.gearyId)
        }
        return true
    }
}

@Serializable
@SerialName("enable")
public data class EnableComponentAction(override val components: Set<String>) : ComponentAction() {
    override fun runOn(entity: GearyEntity): Boolean {
        componentClasses.forEach {
            Engine.enableComponentFor(it, entity.gearyId)
        }
        return true
    }
}
