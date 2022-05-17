@file:OptIn(ExperimentalUnsignedTypes::class)
// Some of these run often enough and are stable enough to justify inlining.
@file:Suppress("NOTHING_TO_INLINE")

package com.mineinabyss.geary.datatypes

//can't make const because of the shl
public val NO_ROLE: ULong = 0uL
public val RELATION: ULong = 1uL shl 63
public val HOLDS_DATA: ULong = 1uL shl 62
//4
//5
//5
//6
//7
//8
//No more bits reserved

public const val TYPE_ROLES_MASK: ULong = 0xFF00000000000000uL
public const val ENTITY_MASK: ULong = 0x00FFFFFFFFFFFFFFuL
public const val RELATION_KIND_MASK: ULong = 0xFFFFFFFF00000000uL
public const val RELATION_TARGET_MASK: ULong = 0x00000000FFFFFFFFuL

public inline fun GearyComponentId.isRelation(): Boolean = this.hasRole(RELATION)
public inline fun GearyComponentId.holdsData(): Boolean = this.hasRole(HOLDS_DATA)

public inline fun GearyComponentId.hasRole(role: ULong): Boolean = this and role != 0uL

public inline fun GearyComponentId.withRole(role: ULong): ULong = this or role

public inline fun GearyComponentId.withoutRole(role: ULong): ULong = this and role.inv()

public inline fun GearyComponentId.withInvertedRole(role: ULong): ULong = this xor role
