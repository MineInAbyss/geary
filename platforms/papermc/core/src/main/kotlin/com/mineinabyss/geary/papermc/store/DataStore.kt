package com.mineinabyss.geary.papermc.store

import com.mineinabyss.geary.context.globalContext
import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.helpers.toGeary
import com.mineinabyss.geary.papermc.globalContextMC
import com.mineinabyss.geary.papermc.helpers.getNamespacedKeyFor
import com.mineinabyss.geary.papermc.helpers.getSerializerFor
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.util.toMCKey
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.SetSerializer
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.bukkit.persistence.PersistentDataType.BYTE_ARRAY

/** Returns whether or not this [PersistentDataContainer] has a component [T] encoded in it. */
public inline fun <reified T : GearyComponent> PersistentDataContainer.has(): Boolean {
    return has(globalContextMC.serializers.getNamespacedKeyFor<T>() ?: return false, BYTE_ARRAY)
}

/**
 * Encodes a component into this [PersistentDataContainer], where the serializer and key can automatically be found via
 * [Formats].
 */
public fun <T : GearyComponent> PersistentDataContainer.encode(
    value: T,
    serializer: SerializationStrategy<T> = ((globalContextMC.serializers.getSerializerFor(value::class)
        ?: error("Serializer not registered for ${value::class.simpleName}")) as SerializationStrategy<T>),
    key: NamespacedKey = globalContextMC.serializers.getSerialNameFor(value::class)?.toComponentKey()
        ?: error("SerialName  not registered for ${value::class.simpleName}"),
) {
    hasComponentsEncoded = true
    val encoded = globalContextMC.formats.binaryFormat.encodeToByteArray(serializer, value)
    this[key, BYTE_ARRAY] = encoded
}

/**
 * Decodes a component of type [T] from this [PersistentDataContainer], where serializer and key are automatically
 * found via [Formats].
 */
//TODO use context when compiler fixed
/**/
public inline fun <reified T : GearyComponent> PersistentDataContainer.decode(): T? {
    return decode(
        serializer = globalContext.serializers.getSerializerFor(T::class) ?: return null,
        key = globalContext.serializers.getSerialNameFor(T::class)?.toComponentKey() ?: return null
    )
}

/**
 * Decodes a component of type [T] from this [PersistentDataContainer] where the [serializer] may automatically be found
 * via [Formats] given a [key].
 */
public inline fun <reified T : GearyComponent> PersistentDataContainer.decode(
    key: NamespacedKey,
    serializer: DeserializationStrategy<out T>? =
        globalContext.serializers.getSerializerFor(key.removeComponentPrefix(), T::class)
): T? {

    serializer ?: return null
    val encoded = get(key, BYTE_ARRAY) ?: return null
    return runCatching { globalContext.formats.binaryFormat.decodeFromByteArray(serializer, encoded) }.getOrNull()
}

/**
 * Encodes a list of [components] to this [PersistentDataContainer].
 *
 * @see encode
 */
public fun PersistentDataContainer.encodeComponents(
    components: Collection<GearyComponent>,
    type: GearyType
) {
    hasComponentsEncoded = true
    //remove all keys present on the PDC so we only end up with the new list of components being encoded
    keys.filter { it.namespace == "geary" && it != globalContextMC.engine.componentsKey }
        .forEach { remove(it) }

    for (value in components)
        encode(value)

    val prefabs = type.filter { it.isInstance() }
    if (prefabs.size != 0)
        encodePrefabs(prefabs.map { it.toGeary().get<PrefabKey>() }.filterNotNull())
}

/**
 * Encodes a list of [PrefabKey]s under the key `geary:prefabs`. When decoding these will be stored in
 * [DecodedEntityData.type].
 */
public fun PersistentDataContainer.encodePrefabs(keys: Collection<PrefabKey>) {
    hasComponentsEncoded = true

    // I prefer being explicit with the SetSerializer to avoid any confusion, like a class that looks like a persisting
    // component that stores a list of prefabs.
    encode(
        keys.toSet(),
        SetSerializer(PrefabKey.serializer()),
        "geary:prefabs".toMCKey()
    )
}

public object PrefabNamespaceMigrations {
    public val migrations: MutableMap<String, String> = mutableMapOf()
}

public fun PersistentDataContainer.decodePrefabs(): Set<PrefabKey> =
    decode("geary:prefabs".toMCKey(), SetSerializer(PrefabKey.serializer()))
        ?.map {
            val key = PrefabKey.of(it.toString())
            // Migrate namespace if needed
            val migrated = PrefabNamespaceMigrations.migrations.getOrDefault(key.namespace, key.namespace)
            PrefabKey.of(migrated, key.key)
        }
        ?.toSet()
        ?: emptySet()

/**
 * Decodes a set of components from this [PersistentDataContainer].
 *
 * @see decode
 */
public fun PersistentDataContainer.decodeComponents(): DecodedEntityData =
    DecodedEntityData(
        // only include keys that start with the component prefix and remove it to get the serial name
        persistingComponents = keys
            .filter { it.key.startsWith(COMPONENT_PREFIX) }
            .mapNotNull { decode(it) }
            .toSet(),
        type = GearyType(decodePrefabs().mapNotNull { it.toEntity()?.id?.withRole(INSTANCEOF) })
    )

/** Verifies a [PersistentDataContainer] has a tag identifying it as containing Geary components. */
public var PersistentDataContainer.hasComponentsEncoded: Boolean
    get() = has(globalContextMC.engine.componentsKey, PersistentDataType.BYTE)
    set(value) {
        when {
            //TODO are there any empty marker keys?
            value -> if (!hasComponentsEncoded) set(globalContextMC.engine.componentsKey, PersistentDataType.BYTE, 1)
            else -> remove(globalContextMC.engine.componentsKey)
        }
    }
