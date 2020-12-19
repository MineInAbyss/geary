package com.mineinabyss.geary.ecs.serialization

import com.mineinabyss.geary.dsl.attachToGeary
import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.types.GearyEntityType
import com.mineinabyss.geary.minecraft.Geary
import kotlinx.serialization.modules.polymorphic

internal fun Geary.registerSerializers() {
    attachToGeary {
        serializers {
            polymorphic(GearyComponent::class) {
                // whenever we're using this system to deserialize our components we want to access them by reference
                // through geary, not by using the actual entity type's serializer like we would when reading config files
                subclass(GearyEntityType::class, GearyEntityType.ByReferenceSerializer)
            }
        }
    }
}
