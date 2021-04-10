package com.mineinabyss.geary.minecraft.store

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.api.engine.type
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.idofront.items.editItemMeta
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataHolder


public fun PersistentDataHolder.decodeComponents(): Pair<Set<GearyComponent>, GearyType> =
    persistentDataContainer.decodeComponents()

public fun PersistentDataHolder.encodeComponents(entity: GearyEntity) {
    persistentDataContainer.encodeComponents(entity.getPersistingComponents(), entity.type)
}

public fun ItemStack.decodeComponents(): Pair<Set<GearyComponent>, GearyType> =
    itemMeta.decodeComponents()

public fun ItemStack.encodeComponents(components: Collection<GearyComponent>) {
    editItemMeta { encodeComponents(components) }
}
