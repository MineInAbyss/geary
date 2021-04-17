package com.mineinabyss.geary.minecraft.components

import org.bukkit.inventory.ItemStack

/**
 * Component for items registered with the ECS.
 *
 * @param item The last known item this entity was associated with.
 * This is not a direct link to an actual item in Minecraft.
 * @param slot The slot number of this item in its inventory.
 */
public data class ItemComponent(
    var item: ItemStack, //TODO should setter be internal?
    var slot: Int
)
