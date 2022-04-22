package com.mineinabyss.geary.papermc.store

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.toGeary
import com.mineinabyss.geary.ecs.components.PersistingComponent
import com.mineinabyss.geary.ecs.engine.isInstance
import com.mineinabyss.geary.prefabs.helpers.addPrefab
import com.mineinabyss.idofront.items.editItemMeta
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataHolder

/** Encodes this entity's persisting components into a [PersistentDataContainer] */
public fun GearyEntity.encodeComponentsTo(pdc: PersistentDataContainer) {
    val persisting = getPersistingComponents()
    if (persisting.isEmpty() && !type.any { it.isInstance() }) {
        pdc.hasComponentsEncoded = false
        return
    }
    // Update hashes
    persisting.forEach {
        getRelation(it::class, PersistingComponent::class)?.hash = it.hashCode()
    }
    pdc.encodeComponents(persisting, type)
}

public fun GearyEntity.encodeComponentsTo(holder: PersistentDataHolder) {
    encodeComponentsTo(holder.persistentDataContainer)
}

public fun GearyEntity.encodeComponentsTo(item: ItemStack) {
    item.editItemMeta {
        encodeComponentsTo(persistentDataContainer)
    }
}


/** Decodes a [PersistentDataContainer]'s components, adding them to this entity and its list of persisting components */
public fun GearyEntity.decodeComponentsFrom(pdc: PersistentDataContainer) {
    decodeComponentsFrom(pdc.decodeComponents())
}

public fun GearyEntity.decodeComponentsFrom(decodedEntityData: DecodedEntityData) {
    val (components, type) = decodedEntityData

    // Components written to this entity's PDC will override the ones defined in type
    setAllPersisting(components)
    type.forEach {
        addPrefab(it.toGeary())
    }
}

public fun PersistentDataHolder.decodeComponents(): DecodedEntityData =
    persistentDataContainer.decodeComponents()

public fun ItemStack.decodeComponents(): DecodedEntityData =
    itemMeta.decodeComponents()
