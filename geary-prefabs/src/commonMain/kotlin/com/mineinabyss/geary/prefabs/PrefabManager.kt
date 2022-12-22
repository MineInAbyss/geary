package com.mineinabyss.geary.prefabs

import com.mineinabyss.geary.components.relations.DontInherit
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.datatypes.Component
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.with
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.prefabs.helpers.inheritPrefabs
import com.mineinabyss.geary.serialization.serialization
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.builtins.ListSerializer
import okio.Path.Companion.toOkioPath
import java.nio.file.Path
import java.util.*
import kotlin.io.path.extension
import kotlin.io.path.nameWithoutExtension

/**
 * Manages registered prefabs and accessing them via name.
 */
class PrefabManager {
    private val formats get() = serialization.formats
    private val logger get() = geary.logger

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

}
