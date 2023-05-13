package com.mineinabyss.geary.prefabs

import com.benasher44.uuid.Uuid
import com.mineinabyss.geary.components.relations.DontInherit
import com.mineinabyss.geary.datatypes.Component
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.with
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.prefabs.helpers.inheritPrefabs
import com.mineinabyss.geary.serialization.dsl.serializableComponents
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.builtins.ListSerializer
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
                    logger.e("Could not read prefab $path:\n\u001B[37m${it.message}")
                }
            }
        }
        logger.i("Loaded ${loaded.count { it.isSuccess }}/${loaded.count()} prefabs")
    }

    /** If this entity has a [Prefab] component, clears it and loads components from its file. */
    fun reread(entity: Entity) {
        entity.with { prefab: Prefab, key: PrefabKey ->
            entity.clear()
            loadFromPath(key.namespace, prefab.file ?: return, entity)
            entity.inheritPrefabs()
        }
    }

    /** Registers an entity with components defined in a [path], adding a [Prefab] component. */
    fun loadFromPath(namespace: String, path: Path, writeTo: Entity? = null): Result<Entity> {
        return runCatching {
            val serializer = ListSerializer(PolymorphicSerializer(Component::class))
            val ext = path.name.substringAfterLast('.')

            val decoded = formats[ext]?.decodeFromFile(serializer, path)
                ?: throw IllegalArgumentException("Unknown file format $ext")
            val entity = writeTo ?: entity()
            entity.set(Prefab(path))
            entity.addRelation<DontInherit, Prefab>()
            entity.addRelation<DontInherit, Uuid>()
            entity.setAll(decoded)

            val key = PrefabKey.of(namespace, path.name.substringBeforeLast('.'))
            entity.set(key)
            entity
        }
    }
}
