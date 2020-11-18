package com.mineinabyss.geary.ecs.components

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.types.EntityTypeManager
import com.mineinabyss.geary.ecs.types.GearyEntityType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("mobzy:statictype")
public class StaticType(
        public val plugin: String,
        public val name: String,
) : GearyComponent() {
    init {
        persist = true
    }

    public val entityType: GearyEntityType by lazy {
        EntityTypeManager.get(plugin)?.get(name) ?: error("Type $plugin:$name not found")
    }
}
