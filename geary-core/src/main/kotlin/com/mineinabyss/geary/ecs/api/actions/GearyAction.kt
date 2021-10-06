package com.mineinabyss.geary.ecs.api.actions

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.properties.EntityPropertyHolder
import kotlinx.serialization.Serializable

/**
 * Actions are pieces of code that can be [run against][invoke] a specific entity.
 *
 * Using polymorphic serialization, we can ask to deserialize a list of GearyActions and allow users to specify
 * specific actions within a config.
 *
 * Because ktx.serialization has very clean support for nesting serializable classes, the goal is to encourage using
 * composition to turn simple one-off actions into highly configurable ones.
 *
 * Please read the wiki for more info on common classes you might want to use for extendable composition.
 */
@Serializable
public abstract class GearyAction : EntityPropertyHolder(), (GearyEntity) -> Boolean {
    public override fun invoke(entity: GearyEntity): Boolean =
        entity.runWithProperties { run() } ?: false

    protected abstract fun GearyEntity.run(): Boolean
}
