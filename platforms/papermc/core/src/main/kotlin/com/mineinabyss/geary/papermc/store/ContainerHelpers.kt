package com.mineinabyss.geary.papermc.store

import com.mineinabyss.geary.ecs.api.FormatsContext
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.toGeary
import com.mineinabyss.geary.ecs.api.systems.QueryContext
import com.mineinabyss.geary.ecs.components.PersistingComponent
import com.mineinabyss.geary.papermc.GearyMCContext
import com.mineinabyss.geary.papermc.PaperEngineContext
import com.mineinabyss.geary.prefabs.helpers.addPrefab
import com.mineinabyss.idofront.items.editItemMeta
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataHolder

/** Encodes this entity's persisting components into a [PersistentDataContainer] */
context(PaperEngineContext, FormatsContext) public fun GearyEntity.encodeComponentsTo(pdc: PersistentDataContainer) {
    val persisting = getPersistingComponents()

    // Update hashes
    persisting.forEach {
        getRelation(it::class, PersistingComponent::class)?.hash = it.hashCode()
    }
    pdc.encodeComponents(persisting, type)
}

context(PaperEngineContext, FormatsContext) public fun GearyEntity.encodeComponentsTo(holder: PersistentDataHolder) {
    encodeComponentsTo(holder.persistentDataContainer)
}

context(PaperEngineContext, FormatsContext) public fun GearyEntity.encodeComponentsTo(item: ItemStack) {
    item.editItemMeta {
        encodeComponentsTo(persistentDataContainer)
    }
}


/** Decodes a [PersistentDataContainer]'s components, adding them to this entity and its list of persisting components */
context(PaperEngineContext, FormatsContext, QueryContext)public fun GearyEntity.decodeComponentsFrom(pdc: PersistentDataContainer) {
    decodeComponentsFrom(pdc.decodeComponents())
}

context(PaperEngineContext, QueryContext) public fun GearyEntity.decodeComponentsFrom(decodedEntityData: DecodedEntityData) {
    val (components, type) = decodedEntityData

    // Components written to this entity's PDC will override the ones defined in type
    setAllPersisting(components)
    type.forEach {
        addPrefab(it.toGeary())
    }
}

context(GearyMCContext) public fun PersistentDataHolder.decodeComponents(): DecodedEntityData =
    persistentDataContainer.decodeComponents()

context(GearyMCContext) public fun ItemStack.decodeComponents(): DecodedEntityData =
    itemMeta.decodeComponents()
