@file:OptIn(ExperimentalUnsignedTypes::class)

package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.GearyComponentId

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
public const val RELATION_PARENT_MASK: ULong = 0x00FFFFFF00000000uL
public const val RELATION_COMPONENT_MASK: ULong = 0xFF000000FFFFFFFFuL

public fun GearyComponentId.isInstance(): Boolean = this.hasRole(INSTANCEOF)
public fun GearyComponentId.isChild(): Boolean = this.hasRole(CHILDOF)
public fun GearyComponentId.isRelation(): Boolean = this.hasRole(RELATION)
public fun GearyComponentId.holdsData(): Boolean = this.hasRole(HOLDS_DATA)

@ExperimentalUnsignedTypes
public fun GearyComponentId.hasRole(role: ULong): Boolean = this and role != 0uL

@ExperimentalUnsignedTypes
public fun GearyComponentId.withRole(role: ULong): ULong = this or role

@ExperimentalUnsignedTypes
public fun GearyComponentId.withoutRole(role: ULong): ULong = this and role.inv()

@ExperimentalUnsignedTypes
public fun GearyComponentId.withInvertedRole(role: ULong): ULong = this xor role
