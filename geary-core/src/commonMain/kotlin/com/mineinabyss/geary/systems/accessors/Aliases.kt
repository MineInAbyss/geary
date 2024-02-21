package com.mineinabyss.geary.systems.accessors

import com.mineinabyss.geary.systems.query.Query
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty

typealias ReadOnlyAccessor<T> = ReadOnlyProperty<Query, T>
typealias ReadWriteAccessor<T> = ReadWriteProperty<Query, T>
