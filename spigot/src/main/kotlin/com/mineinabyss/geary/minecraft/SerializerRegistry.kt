package com.mineinabyss.geary.minecraft

import com.mineinabyss.geary.ecs.actions.*
import com.mineinabyss.geary.ecs.actions.components.*
import com.mineinabyss.geary.ecs.conditions.ComponentConditions
import com.mineinabyss.geary.ecs.conditions.GearyCondition
import com.mineinabyss.geary.ecs.types.GearyEntityType
import com.mineinabyss.geary.minecraft.actions.ApplyPotionAction
import com.mineinabyss.geary.minecraft.actions.DealDamageAction
import com.mineinabyss.geary.minecraft.components.PlayerComponent
import com.mineinabyss.geary.minecraft.conditions.PlayerConditions
import com.mineinabyss.geary.minecraft.dsl.attachToGeary
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.bukkit.entity.Player

//TODO move this into GearyPlugin once we merge auto serializer registry
internal fun GearyPlugin.registerSerializers() {
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
            action(CancelEventAction.serializer())
            action(SwitchToTargetAction.serializer())

            action(AddComponentAction.serializer())
            action(RemoveComponentAction.serializer())
            action(DisableComponentAction.serializer())
            action(EnableComponentAction.serializer())

            action(ApplyPotionAction.serializer())
            action(DealDamageAction.serializer())
        }

        serializers {
            polymorphic(GearyCondition::class) {
                subclass(ComponentConditions.serializer())
                subclass(PlayerConditions.serializer())
            }
        }

        bukkitEntityAccess {
            onEntityRegister<Player> { player ->
                add(PlayerComponent(player.uniqueId))
            }
        }
    }
}
