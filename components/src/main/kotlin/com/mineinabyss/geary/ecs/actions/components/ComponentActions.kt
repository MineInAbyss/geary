package com.mineinabyss.geary.ecs.actions.components

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.actions.GearyAction
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.helper.toComponentClasses
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * > geary:add
 *
 * Adds components to the entity this action is run on.
 */
@Serializable
@SerialName("geary:add")
public class AddComponentAction(
    private val components: Set<@Polymorphic GearyComponent>
) : GearyAction() {
    override fun GearyEntity.run(): Boolean {
        setAll(components)
        return true
    }
}

/**
 * > geary:remove
 *
 * Removes components by serial name from this entity.
 */
@Serializable
@SerialName("geary:remove")
public data class RemoveComponentAction(private val componentNames: Set<String>) : GearyAction() {
    private val componentClasses by lazy { componentNames.toComponentClasses() }

    override fun GearyEntity.run(): Boolean {
        componentClasses.forEach {
            Engine.removeComponentFor(id, componentId(it))
        }
        return true
    }
}
