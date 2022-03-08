package com.mineinabyss.geary.prefabs

import com.mineinabyss.geary.ecs.api.FormatsContext
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.EngineContext
import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.with
import com.mineinabyss.geary.ecs.api.relations.NoInherit
import com.mineinabyss.geary.ecs.serialization.GearyEntitySerializer
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.prefabs.helpers.inheritPrefabs
import com.mineinabyss.idofront.messaging.logError
import com.uchuhimo.collections.MutableBiMap
import com.uchuhimo.collections.mutableBiMapOf
import kotlinx.serialization.json.decodeFromStream
import java.io.File

/**
 * Manages registered prefabs and accessing them via name.
 *
 * @property keys A list of registered [PrefabKey]s.
 */
public class PrefabManager(
    override val engine: Engine
) : EngineContext {
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

    /** If this entity has a [Prefab] component, clears it and loads components from its file. */
    context(FormatsContext)
    public fun reread(entity: GearyEntity) {
        entity.with { prefab: Prefab, key: PrefabKey ->
            entity.clear()
            loadFromFile(key.namespace, prefab.file, entity)
            entity.inheritPrefabs()
        }
    }

    /** Registers an entity with components defined in a [file], adding a [Prefab] component. */
    context(FormatsContext)
    public fun loadFromFile(namespace: String, file: File, writeTo: GearyEntity? = null): GearyEntity? {
        val name = file.nameWithoutExtension
        return runCatching {
            val serializer = GearyEntitySerializer.componentListSerializer
            val decoded = when (val ext = file.extension) {
                "yml" -> formats.yamlFormat.decodeFromStream(serializer, file.inputStream())
                "json" -> formats.jsonFormat.decodeFromStream(serializer, file.inputStream())
                else -> error("Unknown file format $ext")
            }
            val entity = writeTo ?: entity()
            entity.setAll(decoded)

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
