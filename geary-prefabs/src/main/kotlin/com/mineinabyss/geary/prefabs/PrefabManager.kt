package com.mineinabyss.geary.prefabs

import com.mineinabyss.geary.components.relations.DontInherit
import com.mineinabyss.geary.context.GearyContext
import com.mineinabyss.geary.context.GearyContextKoin
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.with
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.prefabs.helpers.inheritPrefabs
import com.mineinabyss.geary.serialization.EntitySerializer
import com.mineinabyss.geary.serialization.Formats
import com.mineinabyss.idofront.messaging.logError
import com.mineinabyss.idofront.messaging.logWarn
import okio.Path.Companion.toOkioPath
import java.io.File
import java.util.*

/**
 * Manages registered prefabs and accessing them via name.
 */
class PrefabManager(
    val formats: Formats
) {
    /** A list of registered [PrefabKey]s. */
    val keys: List<PrefabKey> get() = keyToPrefab.keys.toList()

    private val keyToPrefab: MutableMap<PrefabKey, Entity> = mutableMapOf()

    /** Gets a prefab by [name]. */
    operator fun get(name: PrefabKey): Entity? = keyToPrefab[name]

    /** Registers a prefab with Geary. */
    fun registerPrefab(key: PrefabKey, prefab: Entity) {
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
    fun reread(entity: Entity) {
        entity.with { prefab: Prefab, key: PrefabKey ->
            entity.clear()
            loadFromFile(key.namespace, prefab.file ?: return, entity)
            entity.inheritPrefabs()
        }
    }

    /** Registers an entity with components defined in a [file], adding a [Prefab] component. */
    fun loadFromFile(namespace: String, file: File, writeTo: Entity? = null): Entity? {
        val name = file.nameWithoutExtension
        return runCatching {
            val serializer = EntitySerializer.componentListSerializer
            val ext = file.extension
            val decoded = formats[ext]?.decodeFromFile(serializer, file.toOkioPath())
                ?: error("Unknown file format $ext")
            val entity = writeTo ?: entity()
            entity.set(Prefab(file))
            entity.addRelation<DontInherit, Prefab>()
            entity.addRelation<DontInherit, UUID>()
            entity.setAll(decoded)

            val key = PrefabKey.of(namespace, name)
            registerPrefab(key, entity)
            entity
        }.onFailure {
            logError("Can't read prefab $name from ${file.path}:")
            logWarn(it.toString())
        }.getOrNull()
    }
}
