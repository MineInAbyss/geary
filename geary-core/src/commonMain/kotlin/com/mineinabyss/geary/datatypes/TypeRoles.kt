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

public inline fun ComponentId.isRelation(): Boolean = this.hasRole(RELATION)
public inline fun ComponentId.holdsData(): Boolean = this.hasRole(HOLDS_DATA)

public inline fun ComponentId.hasRole(role: ULong): Boolean = this and role != 0uL
public inline fun Relation.hasRole(role: ULong): Boolean = id.hasRole(role)

public inline fun ComponentId.withRole(role: ULong): ULong = this or role
public inline fun Relation.withRole(role: ULong): Relation = Relation.of(id.withRole(role))

public inline fun ComponentId.withoutRole(role: ULong): ULong = this and role.inv()
public inline fun Relation.withoutRole(role: ULong): Relation = Relation.of(id.withoutRole(role))

public inline fun ComponentId.withInvertedRole(role: ULong): ULong = this xor role
public inline fun Relation.withInvertedRole(role: ULong): Relation = Relation.of(id.withInvertedRole(role))
