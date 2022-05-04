package com.mineinabyss.geary.prefabs

import com.mineinabyss.geary.components.NoInherit
import com.mineinabyss.geary.context.GearyContext
import com.mineinabyss.geary.context.GearyContextKoin
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.with
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.prefabs.helpers.inheritPrefabs
import com.mineinabyss.geary.serialization.GearyEntitySerializer
import com.mineinabyss.idofront.messaging.logError
import okio.Path.Companion.toOkioPath
import java.io.File

/**
 * Manages registered prefabs and accessing them via name.
 */
class PrefabManager : GearyContext by GearyContextKoin() {
    /** A list of registered [PrefabKey]s. */
    val keys: List<PrefabKey> get() = keyToPrefab.keys.toList()

    private val keyToPrefab: MutableMap<PrefabKey, GearyEntity> = mutableMapOf()

    /** Gets a prefab by [name]. */
    operator fun get(name: PrefabKey): GearyEntity? = keyToPrefab[name]

    /** Registers a prefab with Geary. */
    fun registerPrefab(key: PrefabKey, prefab: GearyEntity) {
        keyToPrefab[key] = prefab
        prefab.set(key)
    }

    /** Gets all prefabs registered under a certain [namespace]. */
    fun getPrefabsFor(namespace: String): List<PrefabKey> =
        keys.filter { it.namespace == namespace }

    /** Clears all stored [keyToPrefab] */
    internal fun clear() {
        keyToPrefab.clear()
    }

    /** If this entity has a [Prefab] component, clears it and loads components from its file. */
    fun reread(entity: GearyEntity) {
        entity.with { prefab: Prefab, key: PrefabKey ->
            entity.clear()
            loadFromFile(key.namespace, prefab.file ?: return, entity)
            entity.inheritPrefabs()
        }
    }

    /** Registers an entity with components defined in a [file], adding a [Prefab] component. */
    fun loadFromFile(namespace: String, file: File, writeTo: GearyEntity? = null): GearyEntity? {
        val name = file.nameWithoutExtension
        return runCatching {
            val serializer = GearyEntitySerializer.componentListSerializer
            val ext = file.extension
            val decoded = formats[ext]?.decodeFromFile(serializer, file.toOkioPath())
                ?: error("Unknown file format $ext")
            val entity = writeTo ?: entity()
            entity.set(Prefab(file))
            entity.setRelation(Prefab::class, NoInherit)
            entity.setAll(decoded)

            val key = PrefabKey.of(namespace, name)
            registerPrefab(key, entity)
            entity
        }.onFailure {
            logError("Error deserializing prefab: $name from ${file.path}")
            it.printStackTrace()
        }.getOrNull()
    }
}
