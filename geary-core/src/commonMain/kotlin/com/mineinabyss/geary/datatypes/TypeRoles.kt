// Some of these run often enough and are stable enough to justify inlining.
@file:Suppress("NOTHING_TO_INLINE")

package com.mineinabyss.geary.datatypes

//TODO Currently avoiding using consts because it's a breaking change, check if this is actually worth the perf.
//const val NO_ROLE: ULong = 0uL
//const val RELATION: ULong = 0x8000000000000000uL // 1 shl 63
//const val HOLDS_DATA: ULong = 0x4000000000000000uL // 1 shl 62
val NO_ROLE: ULong = 0uL
val RELATION: ULong = 1uL shl 63
val HOLDS_DATA: ULong = 1uL shl 62
//4
//5
//5
//6
//7
//8
//No more bits reserved

const val TYPE_ROLES_MASK: ULong = 0xFF00000000000000uL
const val ENTITY_MASK: ULong = 0x00FFFFFFFFFFFFFFuL
const val RELATION_KIND_MASK: ULong = 0xFFFFFFFF00000000uL
const val RELATION_TARGET_MASK: ULong = 0x00000000FFFFFFFFuL

inline fun ComponentId.isRelation(): Boolean = this.hasRole(RELATION)
inline fun ComponentId.holdsData(): Boolean = this.hasRole(HOLDS_DATA)

inline fun ComponentId.hasRole(role: ULong): Boolean = this and role != 0uL
inline fun Relation.hasRole(role: ULong): Boolean = id.hasRole(role)

inline fun ComponentId.withRole(role: ULong): ULong = this or role
inline fun Relation.withRole(role: ULong): Relation = Relation.of(id.withRole(role))

inline fun ComponentId.withoutRole(role: ULong): ULong = this and role.inv()
inline fun Relation.withoutRole(role: ULong): Relation = Relation.of(id.withoutRole(role))

inline fun ComponentId.withInvertedRole(role: ULong): ULong = this xor role
inline fun Relation.withInvertedRole(role: ULong): Relation = Relation.of(id.withInvertedRole(role))
