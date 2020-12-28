package com.mineinabyss.geary.ecs.serialization

import com.mineinabyss.geary.dsl.attachToGeary
import com.mineinabyss.geary.ecs.actions.ConditionalAction
import com.mineinabyss.geary.ecs.actions.CooldownAction
import com.mineinabyss.geary.ecs.actions.DebugAction
import com.mineinabyss.geary.ecs.actions.EntityAction
import com.mineinabyss.geary.ecs.actions.components.*
import com.mineinabyss.geary.ecs.types.GearyEntityType
import com.mineinabyss.geary.minecraft.Geary

internal fun Geary.registerSerializers() {
    // This will also register a serializer for GearyEntityType
    attachToGeary<GearyEntityType> {
        components {
            component(Conditions.serializer())
        }

        actions {
            action(DebugAction.serializer())
            action(EntityAction.serializer())
            action(CooldownAction.serializer())
            action(ConditionalAction.serializer())

            action(AddComponentAction.serializer())
            action(RemoveComponentAction.serializer())
            action(DisableComponentAction.serializer())
            action(EnableComponentAction.serializer())

        }
    }
}
