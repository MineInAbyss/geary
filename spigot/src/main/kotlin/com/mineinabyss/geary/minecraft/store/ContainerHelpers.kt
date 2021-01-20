package com.mineinabyss.geary.minecraft.store

import com.mineinabyss.geary.ecs.SerializableGearyComponent
import com.mineinabyss.idofront.items.editItemMeta
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataHolder


public fun PersistentDataHolder.decodeComponents(): Set<SerializableGearyComponent> =
    persistentDataContainer.decodeComponents()

public fun PersistentDataHolder.encodeComponents(components: Collection<SerializableGearyComponent>) {
    persistentDataContainer.encodeComponents(components)
}

public fun ItemStack.decodeComponents(): Set<SerializableGearyComponent> =
    itemMeta.decodeComponents()

public fun ItemStack.encodeComponents(components: Collection<SerializableGearyComponent>) {
    editItemMeta { encodeComponents(components) }
}
