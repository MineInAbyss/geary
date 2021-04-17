package com.mineinabyss.geary.minecraft.store

import com.mineinabyss.geary.ecs.api.engine.type
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.geary
import com.mineinabyss.geary.ecs.engine.INSTANCEOF
import com.mineinabyss.geary.ecs.entities.addPrefab
import com.mineinabyss.geary.ecs.prefab.PrefabKey
import org.bukkit.persistence.PersistentDataContainer
import java.util.*

/** Encodes this entity's persisting components into a [PersistentDataContainer] */
public fun GearyEntity.encodeComponentsTo(pdc: PersistentDataContainer, encodePrefabKey: Boolean = false) {
    val type = type.toCollection(TreeSet())
    if (encodePrefabKey)
        get<PrefabKey>()?.toEntity()?.id?.let {
            type.add(it or INSTANCEOF)
        }
    pdc.encodeComponents(getPersistingComponents(), type)
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
