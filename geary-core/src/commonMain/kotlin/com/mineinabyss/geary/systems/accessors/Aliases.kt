package com.mineinabyss.geary.systems.accessors

import com.mineinabyss.geary.datatypes.RecordPointer
import com.mineinabyss.geary.datatypes.Records
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty


typealias ReadOnlyAccessor<T> = ReadOnlyProperty<AccessorThisRef, T>
typealias ReadWriteAccessor<T> = ReadWriteProperty<AccessorThisRef, T>

internal typealias AccessorThisRef = RecordPointer
typealias Pointer = AccessorThisRef
typealias Pointers = Records
