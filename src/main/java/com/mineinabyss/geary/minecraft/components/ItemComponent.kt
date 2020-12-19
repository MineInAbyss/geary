package com.mineinabyss.geary.minecraft.components

import com.mineinabyss.geary.ecs.GearyComponent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer

public data class ItemComponent(
        var item: ItemStack, //TODO should setter be internal?
        var slot: Int,
) : GearyComponent {

    //TODO move into the entity itself, not component
    /** Serializes the entity's components to an [ItemStack]'s [PersistentDataContainer] */
    /*fun writeToItem(item: ItemStack) {
        //TODO don't clone itemMeta yet another time
        //TODO dont save if no changes found to avoid extra computations
        item.editItemMeta {
            persistentDataContainer.encodeComponents(getComponents())
        }
    }*/
}
