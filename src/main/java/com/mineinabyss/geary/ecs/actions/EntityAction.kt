package com.mineinabyss.geary.ecs.actions

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.components.addComponents
import com.mineinabyss.geary.ecs.engine.Engine
import com.mineinabyss.geary.ecs.engine.entity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("entity")
public class EntityAction(
        private val components: Set<GearyComponent>
) : GearyAction() {
    override fun runOn(entity: GearyEntity): Boolean {
        Engine.entity {
            addComponents(components)
        }
        return true
    }
}
