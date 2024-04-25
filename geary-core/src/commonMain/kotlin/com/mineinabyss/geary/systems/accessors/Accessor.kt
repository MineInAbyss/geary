package com.mineinabyss.geary.systems.accessors

import com.mineinabyss.geary.systems.query.Query
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty

interface Accessor {
    val originalAccessor: Accessor?
}

interface ReadOnlyAccessor<T> : Accessor, ReadOnlyProperty<Query, T>

interface ReadWriteAccessor<T> : ReadOnlyAccessor<T>, ReadWriteProperty<Query, T>
