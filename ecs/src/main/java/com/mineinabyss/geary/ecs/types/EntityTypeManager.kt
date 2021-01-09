package com.mineinabyss.geary.ecs.types


/**
 * A singleton into which [GearyExtension]s register [GearyEntityTypes].
 *
 * When we look for a specific entity type, we can access it by plugin and name.
 *
 * Will likely be converted into a service eventually.
 */
public object EntityTypeManager {
    private val typeMap = hashMapOf<String, GearyEntityTypes<*>>()

    public fun add(key: String, types: GearyEntityTypes<*>) {
        typeMap[key] = types
    }

    public operator fun get(plugin: String): GearyEntityTypes<*>? = typeMap[plugin]

    public operator fun get(plugin: String, type: String): GearyEntityType? = get(plugin)?.get(type)
}
