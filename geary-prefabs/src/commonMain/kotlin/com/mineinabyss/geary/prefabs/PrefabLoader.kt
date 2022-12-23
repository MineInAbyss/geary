package com.mineinabyss.geary.prefabs

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.mineinabyss.geary.components.relations.DontInherit
import com.mineinabyss.geary.datatypes.Component
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.with
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.prefabs.helpers.inheritPrefabs
import com.mineinabyss.geary.serialization.serializableComponents
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.builtins.ListSerializer
import okio.FileSystem
import okio.Path

class PrefabLoader {
    private val manager = prefabs.manager
    private val formats = serializableComponents.formats
    private val logger = geary.logger

    private val readFiles = mutableListOf<PrefabPath>()

    fun addSource(path: PrefabPath) {
        readFiles.add(path)
    }

    internal fun loadPrefabs() {
        val loaded = readFiles.flatMap { read ->
            read.get().mapNotNull { file ->
                loadFromPathOrNull(read.namespace, file)
            }
        }
        logger.i("Loaded ${loaded.size} prefabs")
    }

    /** If this entity has a [Prefab] component, clears it and loads components from its file. */
    fun reread(entity: Entity) {
        entity.with { prefab: Prefab, key: PrefabKey ->
            entity.clear()
            loadFromPathOrNull(key.namespace, prefab.file ?: return, entity)
            entity.inheritPrefabs()
        }
    }

    /** Registers an entity with components defined in a [path], adding a [Prefab] component. */
    fun loadFromPathOrNull(namespace: String, path: Path, writeTo: Entity? = null): Entity? {
        val name = path.name
        return runCatching {
            val serializer = ListSerializer(PolymorphicSerializer(Component::class))
            val ext = path.name.substringAfterLast('.')

            val decoded = formats[ext]?.decodeFromFile(serializer, path)
                ?: error("Unknown file format $ext")
            val entity = writeTo ?: entity()
            entity.set(Prefab(path))
            entity.addRelation<DontInherit, Prefab>()
            entity.addRelation<DontInherit, Uuid>()
            entity.setAll(decoded)

            val key = PrefabKey.of(namespace, name)
            manager.registerPrefab(key, entity)
            entity
        }.onFailure {
            logger.e("Can't read prefab $name from ${path}:")
            logger.w(it.toString())
        }.getOrNull()
    }
}
