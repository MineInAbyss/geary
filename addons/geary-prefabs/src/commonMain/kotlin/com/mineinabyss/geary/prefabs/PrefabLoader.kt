package com.mineinabyss.geary.prefabs

import co.touchlab.kermit.Logger
import com.mineinabyss.geary.components.relations.NoInherit
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.fastForEach
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.prefabs.configuration.components.CopyToInstances
import com.mineinabyss.geary.prefabs.configuration.components.InheritPrefabs
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.prefabs.helpers.inheritPrefabsIfNeeded
import com.mineinabyss.geary.serialization.formats.Formats
import com.mineinabyss.geary.serialization.serializers.PolymorphicListAsMapSerializer
import com.mineinabyss.geary.serialization.serializers.PolymorphicListAsMapSerializer.Companion.provideConfig
import com.mineinabyss.geary.systems.query.Query
import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.modules.SerializersModule
import kotlin.uuid.Uuid

class PrefabLoader(
    val sources: PrefabSources,
    val world: Geary,
    val formats: Formats,
    val logger: Logger,
) {
    private val needsInherit = world.cache(::NeedsInherit)

    fun markAsPrefab(entity: GearyEntity, key: PrefabKey) {
        entity.set(Prefab())
        entity.set<PrefabKey>(key)
        entity.addRelation<NoInherit, Prefab>()
        entity.addRelation<NoInherit, Uuid>()
        entity.addRelation<NoInherit, CopyToInstances>()
    }

    fun loadOrUpdatePrefabs() {
        val results = mutableListOf<String>()
        sources.paths.forEach { prefabsPath ->
            logger.i("Loading prefabs for namespace '${prefabsPath.namespace}'")
            val loaded = buildList {
                addAll(prefabsPath.paths().map { path -> loadFromPathOrReloadExisting(prefabsPath.namespace, path) })
                addAll(
                    prefabsPath.sources().map {
                        load(
                            key = it.key,
                            source = it.source,
                            writeTo = world.getAddon(Prefabs).manager[it.key],
                            formatExt = it.formatExt
                        )
                    }
                )
            }
            val success = loaded.count { it is PrefabLoadResult.Success }
            val warn = loaded.count { it is PrefabLoadResult.Warn }
            val fail = loaded.count { it is PrefabLoadResult.Failure }
            val total = loaded.count()

            results += buildString {
                append("Loaded prefabs in '${prefabsPath.namespace}':")
                if (success > 0) append(" success: $success,")
                if (warn > 0) append(" warn: $warn,")
                if (fail > 0) append(" fail: $fail,")
                append(" total: $total")
            }
        }
        results.forEach { logger.i(it) }
        needsInherit.entities().fastForEach {
            it.inheritPrefabsIfNeeded()
        }
    }

    fun load(
        key: PrefabKey,
        source: Source,
        writeTo: Entity? = null,
        formatExt: String,
    ): PrefabLoadResult {
        var hadMalformed = false
        val decoded = runCatching {
            val config = PolymorphicListAsMapSerializer.Config<Any>(
                whenComponentMalformed = {
                    if (!hadMalformed) logger.e("[$key] Problems reading components")
                    hadMalformed = true
                }
            )
            val serializer = PolymorphicListAsMapSerializer.ofComponents(config)
            val format = formats[formatExt] ?: throw IllegalArgumentException("Unknown file format $formatExt")
            logger.v("Loading prefab $key from $source")

            format.decode(
                serializer,
                source,
                overrideSerializersModule = SerializersModule {
                    provideConfig(config)
                }
            )
        }

        // Stop here if we need to make a new entity
        // For existing prefabs, add all tags except decoded on fail to keep them tracked
        if (writeTo == null) decoded.onFailure { exception ->
            logger.e("[$key] Failed to load prefab")
            exception.printStackTrace()
            return PrefabLoadResult.Failure(exception)
        }

        val entity = writeTo ?: world.entity()
        markAsPrefab(entity, key)
        decoded.getOrNull()?.let { entity.setAll(it) }
        return when {
            hadMalformed -> PrefabLoadResult.Warn(entity)
            else -> PrefabLoadResult.Success(entity)
        }
    }

    /** Registers an entity with components defined in a [path], adding a [Prefab] component. */
    fun loadFromPath(namespace: String, path: Path, writeTo: Entity? = null): PrefabLoadResult {
        val key = PrefabKey.of(namespace, path.name.substringBeforeLast('.'))
        val ext = path.name.substringAfterLast('.')
        logger.d("Loading prefab at path $path")
        return load(key, SystemFileSystem.source(path).buffered(), writeTo, ext).also { result ->
            // Mark path we loaded from to allow for reloading
            if (result is PrefabLoadResult.Success) result.entity.set(Prefab(path))
        }
    }

    fun loadFromPathOrReloadExisting(namespace: String, path: Path): PrefabLoadResult {
        val key = PrefabKey.of(namespace, path.name.substringBeforeLast('.'))
        val existing = world.getAddon(Prefabs).manager[key]
        existing?.clear()
        return loadFromPath(namespace, path, existing)
    }

    /** If this entity has a [Prefab] component, clears it and loads components from its file. */
    fun reload(entity: Entity) {
        val prefab = entity.get<Prefab>() ?: error("Entity was not an already loaded prefab")
        val key = entity.get<PrefabKey>() ?: error("Entity did not have a prefab key")
        val file = prefab.file ?: error("Prefab did not have a file")
        entity.clear()
        loadFromPath(key.namespace, file, entity)
        entity.inheritPrefabsIfNeeded()
    }

    sealed class PrefabLoadResult {
        data class Success(val entity: Entity) : PrefabLoadResult()
        data class Warn(val entity: Entity) : PrefabLoadResult()
        data class Failure(val error: Throwable) : PrefabLoadResult()
    }

    class NeedsInherit(world: Geary) : Query(world) {
        val inheritPrefabs by get<InheritPrefabs>()
    }
}
