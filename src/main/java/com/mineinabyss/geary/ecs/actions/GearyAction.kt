package com.mineinabyss.geary.ecs.actions

import com.mineinabyss.geary.ecs.GearyEntity
import kotlinx.serialization.Serializable

/**
 * Actions are pieces of code that can be [run against][runOn] a specific entity.
 *
 * They use polymorphic serialization to allow for extendability, while letting us define them within serialized
 * configs.
 *
 * Actions can either be reusable, including many builtin ones, or they may be one-off snippets of code that should run
 * when an event occurs. Because ktx.serialization has very clean support for nesting serializable classes, the
 * goal is to encourage using composition to turn simple one-off actions into highly configurable ones with little work.
 *
 * We provide several useful serializable classes such as [ConfigurableLocation] to help with this. As well, there are
 * many builtin classes such as [CooldownAction] or [ConditionalAction] which can be nested by the end user for even
 * more simple customization that doesn't need to be thought of ahead of time.
 */
@Serializable
public abstract class GearyAction {
    public abstract fun runOn(entity: GearyEntity): Boolean
}

