package com.mineinabyss.geary.minecraft.store

import com.mineinabyss.geary.ecs.accessors.GearyAccessorScope
import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.toGeary
import com.mineinabyss.geary.ecs.components.PersistingComponent
import com.mineinabyss.geary.ecs.engine.isInstance
import com.mineinabyss.geary.minecraft.engine.PaperEngine
import com.mineinabyss.geary.minecraft.hasComponentsEncoded
import com.mineinabyss.idofront.items.editItemMeta
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataHolder
import java.util.*

public class PaperStoreScope(engine: PaperEngine) : GearyAccessorScope(engine) {

    /** Encodes this entity's persisting components into a [PersistentDataContainer] */
    public fun GearyEntity.encodeComponentsTo(pdc: PersistentDataContainer) {
        val persisting = getPersistingComponents()

        //Update hashes
        persisting.forEach {
            getRelation(PersistingComponent::class, it::class)?.hash = it.hashCode()
        }

        pdc.encodeComponents(persisting, type.toCollection(TreeSet()))
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

        //components written to this entity's PDC will override the ones defined in type
        setAllPersisting(components)
        for (id in type) {
            addPrefab(id.toGeary())
        }
    }

    public fun PersistentDataHolder.decodeComponents(): DecodedEntityData =
        persistentDataContainer.decodeComponents()

    public fun ItemStack.decodeComponents(): DecodedEntityData =
        itemMeta.decodeComponents()

    /**
     * Decodes a set of components from this [PersistentDataContainer].
     *
     * @see decode
     */
    public fun PersistentDataContainer.decodeComponents(): DecodedEntityData =
        DecodedEntityData(
            // only include keys that start with the component prefix and remove it to get the serial name
            persistingComponents = keys
                .filter { it.key.startsWith(COMPONENT_PREFIX) }
                .mapNotNull {
                    decode(it)
                }
                .toSet(),
            type = decodePrefabs().mapNotNullTo(sortedSetOf()) { engine.prefabManager[it]?.id }
        )


    /**
     * Encodes a list of [components] to this [PersistentDataContainer].
     *
     * @see encode
     */
    public fun PersistentDataContainer.encodeComponents(components: Collection<GearyComponent>, type: GearyType) {
        hasComponentsEncoded = true
        //remove all keys present on the PDC so we only end up with the new list of components being encoded
        keys.filter { it.namespace == "geary" && it != PaperEngine.componentsKey }.forEach { remove(it) }

        for (value in components)
            encode(value)

        val prefabs = type.filter { it.isInstance() }
        if (prefabs.isNotEmpty())
            encodePrefabs(prefabs.mapNotNull { it.toGeary().get() })
    }
}