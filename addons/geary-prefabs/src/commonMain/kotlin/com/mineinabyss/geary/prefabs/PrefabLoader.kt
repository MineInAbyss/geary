package com.mineinabyss.geary.prefabs

import co.touchlab.kermit.Severity
import com.benasher44.uuid.Uuid
import com.mineinabyss.geary.components.relations.NoInherit
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.prefabs.helpers.inheritPrefabs
import com.mineinabyss.geary.prefabs.serializers.ComponentListAsMapSerializer
import com.mineinabyss.geary.serialization.dsl.serializableComponents
import okio.Path

class PrefabLoader {
    private val manager get() = prefabs.manager
    private val formats get() = serializableComponents.formats

    private val logger get() = geary.logger

    private val readFiles = mutableListOf<PrefabPath>()

    fun addSource(path: PrefabPath) {
        readFiles.add(path)
    }

    internal fun loadPrefabs() {
        val loaded = readFiles.flatMap { read ->
            read.get().map { path ->
                loadFromPath(read.namespace, path).onFailure {
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
    fun reread(entity: Entity) {
        val prefab = entity.get<Prefab>() ?: error("Entity was not an already loaded prefab")
        val key = entity.get<PrefabKey>() ?: error("Entity did not have a prefab key")
        val file = prefab.file ?: error("Prefab did not have a file")
        entity.clear()

        // set basics here as well in case load fails
        entity.addRelation<NoInherit, Prefab>()
        entity.addRelation<NoInherit, Uuid>()
        entity.set(key)
        entity.set(prefab)
        loadFromPath(key.namespace, file, entity).getOrThrow()
        entity.inheritPrefabs()
    }

    /** Registers an entity with components defined in a [path], adding a [Prefab] component. */
    fun loadFromPath(namespace: String, path: Path, writeTo: Entity? = null): Result<Entity> {
        return runCatching {
            val serializer = ComponentListAsMapSerializer()
            val ext = path.name.substringAfterLast('.')
            val decoded = formats[ext]?.decodeFromFile(serializer, path)
                ?: throw IllegalArgumentException("Unknown file format $ext")

            val key = PrefabKey.of(namespace, path.name.substringBeforeLast('.'))

            val entity = writeTo ?: entity()
            entity.addRelation<NoInherit, Prefab>()
            entity.addRelation<NoInherit, Uuid>()
            entity.set(Prefab(path))
            entity.setAll(decoded)
            entity.set(key)
            entity
        }
    }
}
