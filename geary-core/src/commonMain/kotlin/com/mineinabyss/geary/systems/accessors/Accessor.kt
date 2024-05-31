package com.mineinabyss.geary.systems.accessors

import com.mineinabyss.geary.systems.query.Query
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface Accessor {
    val originalAccessor: Accessor?
}

interface ReadOnlyAccessor<out T> : Accessor, ReadOnlyProperty<Query, T> {
    fun get(query: Query): T


    override fun getValue(thisRef: Query, property: KProperty<*>): T {
        return get(thisRef)
    }
}

interface ReadWriteAccessor<T> : ReadOnlyAccessor<T>, ReadWriteProperty<Query, T> {
    fun set(query: Query, value: T)

    override fun getValue(thisRef: Query, property: KProperty<*>): T {
        return get(thisRef)
    }

    override fun setValue(thisRef: Query, property: KProperty<*>, value: T) {
        return set(thisRef, value)
    }
}
