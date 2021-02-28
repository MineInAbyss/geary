package com.mineinabyss.geary.minecraft.store

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.idofront.items.editItemMeta
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataHolder


public fun PersistentDataHolder.decodeComponents(): Set<GearyComponent> =
    persistentDataContainer.decodeComponents()

public fun PersistentDataHolder.encodeComponents(components: Collection<GearyComponent>) {
    persistentDataContainer.encodeComponents(components)
}

public fun ItemStack.decodeComponents(): Set<GearyComponent> =
    itemMeta.decodeComponents()

public fun ItemStack.encodeComponents(components: Collection<GearyComponent>) {
    editItemMeta { encodeComponents(components) }
}
