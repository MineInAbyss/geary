package com.mineinabyss.geary.minecraft.store

import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.components.addPersistingComponents
import com.mineinabyss.geary.ecs.components.getPersistingComponents
import com.mineinabyss.geary.ecs.type
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataHolder

/** Encodes this entity's persisting components into a [PersistentDataContainer] */
public fun GearyEntity.encodeComponents(pdc: PersistentDataContainer) {
    pdc.encodeComponents(getPersistingComponents())
}

/** Decodes a [PersistentDataContainer]'s components, adding them to this entity and its list of persisting components */
public fun GearyEntity.decodeComponents(pdc: PersistentDataContainer) {
    type?.decodeComponentsTo(this)
    //currently components written to this entity's PDC will override the ones defined in type
    addPersistingComponents(pdc.decodeComponents())
}

/** Encodes this entity's persisting components into its [PersistentDataContainer] */
public fun <T> T.encodeComponents() where T : GearyEntity, T : PersistentDataHolder {
    encodeComponents(persistentDataContainer)
}

/**
 * Decodes this entity's [PersistentDataContainer]'s components, adding them to this entity and its list of
 * persisting components.
 */
public fun <T> T.decodeComponents() where T : GearyEntity, T : PersistentDataHolder {
    decodeComponents(persistentDataContainer)
}
