package com.mineinabyss.geary.ecs.types


abstract class GearyEntityTypes<T : GearyEntityType> {
    val types get() = _types.keys.toList()

    private val _types: MutableMap<String, T> = mutableMapOf()
    private val persistentTypes: MutableMap<String, T> = mutableMapOf()

    open fun String.toEntityTypeName(): String = this

    operator fun get(name: String): T = _types[name.toEntityTypeName()]
            ?: error("Static entity template for $name not found")

    /** Gets the entity name from a type [T] if registered, otherwise throws an [IllegalArgumentException]*/
    fun getNameForTemplate(type: GearyEntityType): String =
            (_types.entries.find { type === it.value }?.key
                    ?: error("Static entity template was accessed but not registered in any configuration"))

    /** Registers entity types with the plugin. These will be cleared after a command reload is triggered. */
    internal fun registerType(name: String, type: T) {
        _types[name] = type
    }

    /** Registers entity types with the plugin. These will be cleared after a command reload is triggered. */
    internal fun registerTypes(types: Map<String, T>) {
        this._types += types
    }

    /** Registers persistent entity types with the plugin which do not get cleared after a command reload is triggered. */
    fun registerPersistentType(mob: String, type: T) {
        val entityName = mob.toEntityTypeName()
        _types[entityName] = type
        persistentTypes[entityName] = type
    }

    /** Clears all stored [_types], but not [persistentTypes] */
    internal fun reset() {
        _types.clear()
        _types += persistentTypes
    }
}
