package com.mineinabyss.geary.ecs.prefab

import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.with
import com.mineinabyss.geary.ecs.api.relations.NoInherit
import com.mineinabyss.geary.ecs.components.Prefab
import com.mineinabyss.geary.ecs.prefab.PrefabManager.keys
import com.mineinabyss.geary.ecs.serialization.Formats
import com.mineinabyss.geary.ecs.serialization.GearyEntitySerializer
import com.mineinabyss.idofront.messaging.logError
import com.uchuhimo.collections.MutableBiMap
import com.uchuhimo.collections.mutableBiMapOf
import java.io.File

/**
 * Manages registered prefabs and accessing them via name.
 *
 * @property keys A list of registered [PrefabKey]s.
 */
public object PrefabManager {
    public val keys: List<PrefabKey> get() = prefabs.keys.toList()

    private val prefabs: MutableBiMap<PrefabKey, GearyEntity> = mutableBiMapOf()

    /** Gets a prefab by [name]. */
    public operator fun get(name: PrefabKey): GearyEntity? = prefabs[name]

    /** Registers a prefab with Geary. */
    public fun registerPrefab(key: PrefabKey, prefab: GearyEntity) {
        prefabs[key] = prefab
        prefab.set(key)
    }

    public fun getPrefabsFor(namespace: String): List<PrefabKey> =
        keys.filter { it.namespace == namespace }

    /** Clears all stored [prefabs] */
    internal fun clear() {
        prefabs.clear()
    }

    public fun reread(entity: GearyEntity) {
        entity.with { prefab: Prefab, key: PrefabKey ->
            entity.clear()
            loadFromFile(key.namespace, prefab.file, entity)
            entity.inheritPrefabs()
        }
    }

    public fun loadFromFile(namespace: String, file: File, writeTo: GearyEntity? = null): GearyEntity? {
        val name = file.nameWithoutExtension
        return runCatching {
            val format = when (val ext = file.extension) {
                "yml" -> Formats.yamlFormat
                "json" -> Formats.jsonFormat
                else -> error("Unknown file format $ext")
            }
            val entity = writeTo ?: Engine.entity()
            entity.setAll(format.decodeFromString(GearyEntitySerializer.componentListSerializer, file.readText()))

            val key = PrefabKey.of(namespace, name)
            entity.set(Prefab(file))
            entity.setRelation(Prefab::class, NoInherit)
            registerPrefab(key, entity)
            entity
        }.onFailure {
            logError("Error deserializing prefab: $name from ${file.path}")
            it.printStackTrace()
        }.getOrNull()
    }
}
