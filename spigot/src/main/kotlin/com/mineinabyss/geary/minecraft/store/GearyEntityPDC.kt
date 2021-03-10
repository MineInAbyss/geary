package com.mineinabyss.geary.minecraft.store

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.components.GearyPrefab
import org.bukkit.persistence.PersistentDataContainer


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

    //TODO implement adding prefabs
    //if there's a prefab reference on the PDC, we need to add it before we try and decode components from it.
//    components.asSequence().filterIsInstance<Prefabs>().firstOrNull()?.prefabs?.forEach { addPrefab(it) }

//    type?.decodeComponentsTo(this)

    //components written to this entity's PDC will override the ones defined in type
    setAllPersisting(components)
}
