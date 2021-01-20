package com.mineinabyss.geary.ecs.actions

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.components.addComponents
import com.mineinabyss.geary.ecs.engine.Engine
import com.mineinabyss.geary.ecs.engine.entity
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * An action that spawns a new [GearyEntity] with certain [components].
 *
 * @param components The components to spawn the new entity with. Will not be copied.
 */
//TODO add another list of components that get copied upon creation.
@Serializable
@SerialName("entity")
public class EntityAction(
        private val components: Set<@Contextual GearyComponent>
) : GearyAction() {
    override fun runOn(entity: GearyEntity): Boolean {
        Engine.entity {
            addComponents(components)
        }
        return true
    }
}
