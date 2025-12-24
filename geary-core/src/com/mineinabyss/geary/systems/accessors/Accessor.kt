package com.mineinabyss.geary.systems.accessors

import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.systems.query.Query
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface Accessor<out T> {
    fun load(archetype: Archetype)
}

interface ReadOnlyAccessor<out T> : Accessor<T> {
    operator fun get(row: Int): T
}

interface ReadWriteAccessor<T> : ReadOnlyAccessor<T> {
    operator fun set(row: Int, value: T)
}
