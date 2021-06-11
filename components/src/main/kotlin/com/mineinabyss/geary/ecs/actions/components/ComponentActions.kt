package com.mineinabyss.geary.ecs.actions.components

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.actions.GearyAction
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.helper.Components
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

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
public data class RemoveComponentAction(private val components: Set<String>) : GearyAction() {
    @Transient
    private val _components = Components(components)

    override fun GearyEntity.run(): Boolean {
        _components.classes.forEach {
            Engine.removeComponentFor(id, componentId(it))
        }
        return true
    }
}