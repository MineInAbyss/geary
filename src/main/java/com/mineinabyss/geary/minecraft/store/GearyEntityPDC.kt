package com.mineinabyss.geary.minecraft.store

import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.components.addPersistingComponents
import com.mineinabyss.geary.ecs.components.getPersistingComponents
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataHolder

/** Encodes this entity's persisting components into a [PersistentDataContainer] */
public fun GearyEntity.encodeComponents(pdc: PersistentDataContainer) {
    pdc.encodeComponents(getPersistingComponents())
}

/** Decodes a [PersistentDataContainer]'s components, adding them to this entity and its list of persisting components */
public fun GearyEntity.decodeComponents(pdc: PersistentDataContainer) {
    addPersistingComponents(pdc.decodeComponents())
    //TODO check for static type component and add its components
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
