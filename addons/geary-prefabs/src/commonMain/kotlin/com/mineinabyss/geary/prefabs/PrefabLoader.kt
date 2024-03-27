package com.mineinabyss.geary.prefabs

import co.touchlab.kermit.Severity
import com.benasher44.uuid.Uuid
import com.mineinabyss.geary.components.relations.NoInherit
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.GearyComponent
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.prefabs.configuration.components.CopyToInstances
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.prefabs.helpers.inheritPrefabs
import com.mineinabyss.geary.serialization.dsl.serializableComponents
import com.mineinabyss.geary.serialization.serializers.PolymorphicListAsMapSerializer
import kotlinx.serialization.PolymorphicSerializer
import okio.Path

class PrefabLoader {
    private val manager get() = prefabs.manager
    private val formats get() = serializableComponents.formats

    private val logger get() = geary.logger

    private val readFiles = mutableListOf<PrefabPath>()

    fun addSource(path: PrefabPath) {
        readFiles.add(path)
    }

    fun loadOrUpdatePrefabs() {
        val loaded = readFiles.flatMap { read ->
            read.get().map { path ->
                runCatching { loadFromPathOrReloadExisting(read.namespace, path) }.onFailure {
                    if (logger.config.minSeverity <= Severity.Debug)
                        logger.e("Could not read prefab $path:\n\u001B[37m${it.stackTraceToString()}")
                    else
                        logger.e("Could not read prefab $path:\n\u001B[37m${it.message}")
                }
            }
        }
        loaded.forEach { it.getOrNull()?.inheritPrefabs() }
        logger.i("Loaded ${loaded.count { it.isSuccess }}/${loaded.count()} prefabs")
    }

    /** If this entity has a [Prefab] component, clears it and loads components from its file. */
    fun reload(entity: Entity) {
        val prefab = entity.get<Prefab>() ?: error("Entity was not an already loaded prefab")
        val key = entity.get<PrefabKey>() ?: error("Entity did not have a prefab key")
        val file = prefab.file ?: error("Prefab did not have a file")
        entity.clear()
        loadFromPath(key.namespace, file, entity)
        entity.inheritPrefabs()
    }

    /** Registers an entity with components defined in a [path], adding a [Prefab] component. */
    fun loadFromPath(namespace: String, path: Path, writeTo: Entity? = null): Entity {
        val decoded = runCatching {
            val serializer = PolymorphicListAsMapSerializer.of(PolymorphicSerializer(GearyComponent::class))
            val ext = path.name.substringAfterLast('.')
            logger.d("Loading prefab at $path")
            formats[ext]?.decodeFromFile(serializer, path)
                ?: throw IllegalArgumentException("Unknown file format $ext")
        }
        // Stop here if we need to make a new entity
        // For existing prefabs, add all tags except decoded on fail to keep them tracked
        if (writeTo == null && decoded.isFailure) decoded.getOrThrow()

        val key = PrefabKey.of(namespace, path.name.substringBeforeLast('.'))
        val entity = writeTo ?: entity()
        entity.addRelation<NoInherit, Prefab>()
        entity.addRelation<NoInherit, Uuid>()
        entity.addRelation<NoInherit, CopyToInstances>()
        entity.set(Prefab(path))
        decoded.getOrNull()?.let { entity.setAll(it) }
        entity.set(key)
        decoded.getOrThrow()
        return entity
    }

    fun loadFromPathOrReloadExisting(namespace: String, path: Path): Entity {
        val existing = prefabs.manager[PrefabKey.of(namespace, path.name.substringBeforeLast('.'))]
        existing?.clear()
        return loadFromPath(namespace, path, existing)
    }
}
