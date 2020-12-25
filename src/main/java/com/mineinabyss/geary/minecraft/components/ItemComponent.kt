package com.mineinabyss.geary.minecraft.components

import com.mineinabyss.geary.ecs.GearyComponent
import org.bukkit.inventory.ItemStack

public data class ItemComponent(
        var item: ItemStack, //TODO should setter be internal?
        var slot: Int
) : GearyComponent
