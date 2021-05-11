package com.mineinabyss.geary.minecraft.store

import com.mineinabyss.geary.ecs.api.engine.type
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.geary
import com.mineinabyss.geary.ecs.entities.addPrefab
import org.bukkit.persistence.PersistentDataContainer
import java.util.*

/** Encodes this entity's persisting components into a [PersistentDataContainer] */
public fun GearyEntity.encodeComponentsTo(pdc: PersistentDataContainer) {
    pdc.encodeComponents(getPersistingComponents(), type.toCollection(TreeSet()))
}

/** Decodes a [PersistentDataContainer]'s components, adding them to this entity and its list of persisting components */
public fun GearyEntity.decodeComponentsFrom(pdc: PersistentDataContainer) {
    val (components, type) = pdc.decodeComponents()

    //components written to this entity's PDC will override the ones defined in type
    setAllPersisting(components)
    for (id in type) {
        addPrefab(geary(id))
    }
}
