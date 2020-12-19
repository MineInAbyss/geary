package com.mineinabyss.geary.minecraft

import com.mineinabyss.geary.dsl.attachToGeary
import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.types.GearyEntityType
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass


internal fun Geary.registerSerializers() {
    attachToGeary {
        serializers {
            polymorphic(GearyComponent::class) {
                subclass(GearyEntityType.serializer())
            }
        }
    }
}
