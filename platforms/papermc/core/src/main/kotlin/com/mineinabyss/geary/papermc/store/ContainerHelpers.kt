package com.mineinabyss.geary.papermc.store

import com.mineinabyss.geary.components.relations.Persists
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.datatypes.isInstance
import com.mineinabyss.geary.helpers.toGeary
import com.mineinabyss.geary.prefabs.helpers.addPrefab
import com.mineinabyss.idofront.items.editItemMeta
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataHolder

/** Encodes this entity's persisting components into a [PersistentDataContainer] */
fun GearyEntity.encodeComponentsTo(pdc: PersistentDataContainer) {
    val persisting = getAllPersisting()
    if (persisting.isEmpty() && !type.any { it.isInstance() }) {
        pdc.hasComponentsEncoded = false
        return
    }
    // Update hashes
    persisting.forEach {
        getRelation(it::class, Persists::class)?.hash = it.hashCode()
    }
    pdc.encodeComponents(persisting, type)
}

fun GearyEntity.encodeComponentsTo(holder: PersistentDataHolder) {
    encodeComponentsTo(holder.persistentDataContainer)
}

fun GearyEntity.encodeComponentsTo(item: ItemStack) {
    item.editItemMeta {
        encodeComponentsTo(persistentDataContainer)
    }
}


/** Decodes a [PersistentDataContainer]'s components, adding them to this entity and its list of persisting components */
fun GearyEntity.decodeComponentsFrom(pdc: PersistentDataContainer) {
    decodeComponentsFrom(pdc.decodeComponents())
}

fun GearyEntity.decodeComponentsFrom(decodedEntityData: DecodedEntityData) {
    val (components, type) = decodedEntityData

    // Components written to this entity's PDC will override the ones defined in type
    setAllPersisting(components)
    type.forEach {
        addPrefab(it.toGeary())
    }
}

fun PersistentDataHolder.decodeComponents(): DecodedEntityData =
    persistentDataContainer.decodeComponents()

fun ItemStack.decodeComponents(): DecodedEntityData =
    itemMeta.decodeComponents()
