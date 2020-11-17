package com.mineinabyss.geary.ecs.types

import org.bukkit.plugin.Plugin

/**
 * Manages registered [GearyEntityType]s and accessing them via name, bukkit entity, etc...
 *
 * @property types A map of [GearyEntityType]s registered with the plugin.
 * @property persistentTypes A map of types similar to [types], which persists (command) reloads. Useful for other
 * plugins which register types via code and can't re-register them easily.
 */
abstract class GearyEntityTypes<T : GearyEntityType>(
        val plugin: Plugin
) {
    val types: List<String> get() = _types.keys.toList()

    private val _types: MutableMap<String, T> = mutableMapOf()
    private val persistentTypes: MutableMap<String, T> = mutableMapOf()

    /** When accessing a type by name, will convert the input to follow a defined pattern. */
    open fun String.toEntityTypeName(): String = this

    operator fun get(name: String): T = _types[name.toEntityTypeName()]
            ?: error("Static entity template for $name not found")

    /** Gets the entity name from a type [T] if registered, otherwise throws an [IllegalArgumentException]*/
    fun getNameForTemplate(type: GearyEntityType): String =
            (_types.entries.find { type === it.value }?.key
                    ?: error("Static entity template was accessed but not registered in any configuration"))

    //TODO perhaps better immutability
    /** Registers entity types with the plugin. These will be cleared after a command reload is triggered. */
    fun registerType(name: String, type: T) {
        _types[name] = type
    }

    /** Registers entity types with the plugin. These will be cleared after a command reload is triggered. */
    fun registerTypes(types: Map<String, T>) {
        this._types += types
    }

    /** Registers persistent entity types with the plugin which do not get cleared after a command reload is triggered. */
    fun registerPersistentType(mob: String, type: T) {
        val entityName = mob.toEntityTypeName()
        _types[entityName] = type
        persistentTypes[entityName] = type
    }

    /** Clears all stored [_types], but not [persistentTypes] */
    fun reset() {
        _types.clear()
        _types += persistentTypes
    }
}
