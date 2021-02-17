package com.mineinabyss.geary.minecraft.store

import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.components.addPersistingComponent
import com.mineinabyss.geary.ecs.components.addPersistingComponents
import com.mineinabyss.geary.ecs.components.getPersistingComponents
import com.mineinabyss.geary.ecs.prefab.GearyPrefab
import com.mineinabyss.geary.ecs.type
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataHolder


/** Encodes this [GearyPrefab]'s persisting components to a [PersistentDataContainer]. */
public fun GearyPrefab.encodeComponentsTo(pdc: PersistentDataContainer) {
    pdc.encodeComponents(instantiatePersistingComponents())
}

/** Encodes this entity's persisting components into a [PersistentDataContainer] */
public fun GearyEntity.encodeComponentsTo(pdc: PersistentDataContainer) {
    pdc.encodeComponents(getPersistingComponents())
}

/** Decodes a [PersistentDataContainer]'s components, adding them to this entity and its list of persisting components */
public fun GearyEntity.decodeComponentsFrom(pdc: PersistentDataContainer) {
    val components = pdc.decodeComponents()

    //if there's a prefab reference on the PDC, we need to add it before we try and decode components from it.
    components.asSequence().filterIsInstance<GearyPrefab>().firstOrNull()?.let {
        addPersistingComponent(it)
    }
    type?.decodeComponentsTo(this)

    //components written to this entity's PDC will override the ones defined in type
    addPersistingComponents(components)
}

/** Encodes this entity's persisting components into its [PersistentDataContainer] */
public fun <T> T.encodeComponents() where T : GearyEntity, T : PersistentDataHolder {
    encodeComponentsTo(persistentDataContainer)
}

/**
 * Decodes this entity's [PersistentDataContainer]'s components, adding them to this entity and its list of
 * persisting components.
 */
public fun <T> T.decodeComponents() where T : GearyEntity, T : PersistentDataHolder {
    decodeComponentsFrom(persistentDataContainer)
}
