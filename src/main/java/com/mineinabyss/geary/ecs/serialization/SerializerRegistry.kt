package com.mineinabyss.geary.ecs.serialization

import com.mineinabyss.geary.dsl.attachToGeary
import com.mineinabyss.geary.ecs.types.GearyEntityType
import com.mineinabyss.geary.minecraft.Geary

internal fun Geary.registerSerializers() {
    // This will register a serializer for GearyEntityType
    attachToGeary<GearyEntityType> {
    }
}
