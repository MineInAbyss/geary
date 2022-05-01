@file:OptIn(ExperimentalUnsignedTypes::class)
// Some of these run often enough and are stable enough to justify inlining.
@file:Suppress("NOTHING_TO_INLINE")

package com.mineinabyss.geary.datatypes

//can't make const because of the shl
public val NO_ROLE: ULong = 0uL
public val INSTANCEOF: ULong = 1uL shl 63
public val CHILDOF: ULong = 1uL shl 62
public val RELATION: ULong = 1uL shl 61
public val HOLDS_DATA: ULong = 1uL shl 60
//5
//6
//7
//8
//No more bits that can be used

public const val TYPE_ROLES_MASK: ULong = 0xFF00000000000000uL
public const val ENTITY_MASK: ULong = 0x00FFFFFFFFFFFFFFuL
public const val RELATION_VALUE_MASK: ULong = 0x00FFFFFF00000000uL
public const val RELATION_KEY_MASK: ULong = 0xFF000000FFFFFFFFuL

public inline fun GearyComponentId.isInstance(): Boolean = this.hasRole(INSTANCEOF)
public inline fun GearyComponentId.isChild(): Boolean = this.hasRole(CHILDOF)
public inline fun GearyComponentId.isRelation(): Boolean = this.hasRole(RELATION)
public inline fun GearyComponentId.holdsData(): Boolean = this.hasRole(HOLDS_DATA)

public inline fun GearyComponentId.hasRole(role: ULong): Boolean = this and role != 0uL

public inline fun GearyComponentId.withRole(role: ULong): ULong = this or role

public inline fun GearyComponentId.withoutRole(role: ULong): ULong = this and role.inv()

public inline fun GearyComponentId.withInvertedRole(role: ULong): ULong = this xor role
