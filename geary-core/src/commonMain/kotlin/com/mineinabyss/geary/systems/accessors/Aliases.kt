package com.mineinabyss.geary.systems.accessors

import com.mineinabyss.geary.datatypes.RecordPointer
import com.mineinabyss.geary.datatypes.Records
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty


typealias ReadOnlyAccessor<T> = ReadOnlyProperty<Pointer, T>
typealias ReadWriteAccessor<T> = ReadWriteProperty<Pointer, T>

/** A pointer to where a specific entity's data is stored for use by accessors. */
typealias Pointer = RecordPointer

/** A list of [Pointer]s, currently used to select between the target, source, and event entity in listeners. */
typealias Pointers = Records

typealias GearyPointer = RecordPointer
typealias GearyPointers = Records
