package com.mineinabyss.geary.ecs.actions.components

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.actions.GearyAction
import com.mineinabyss.geary.ecs.components.addComponents
import com.mineinabyss.geary.ecs.engine.Engine
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("add")
public class AddComponentAction(
        private val components: Set<GearyComponent>
) : GearyAction() {
    override fun runOn(entity: GearyEntity) {
        entity.addComponents(components)
    }
}

@Serializable
@SerialName("remove")
public data class RemoveComponentAction(override val components: Set<String>) : ComponentAction() {
    override fun runOn(entity: GearyEntity) {
        componentClasses.forEach {
            Engine.removeComponentFor(it, entity.gearyId)
        }
    }
}

@Serializable
@SerialName("disable")
public class DisableComponentAction(override val components: Set<String>) : ComponentAction() {
    override fun runOn(entity: GearyEntity) {
        componentClasses.forEach {
            Engine.disableComponentFor(it, entity.gearyId)
        }
    }
}

@Serializable
@SerialName("enable")
public data class EnableComponentAction(override val components: Set<String>) : ComponentAction() {
    override fun runOn(entity: GearyEntity) {
        componentClasses.forEach {
            Engine.enableComponentFor(it, entity.gearyId)
        }
    }
}
