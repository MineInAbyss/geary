@file:OptIn(ExperimentalUnsignedTypes::class)

package com.mineinabyss.geary.ecs.engine

public val INSTANCEOF: ULong = 1uL shl 63
public val CHILDOF: ULong = 1uL shl 62
public val TRAIT: ULong = 1uL shl 61
public val HOLDS_DATA: ULong = 1uL shl 60
//5
//6
//7
//8
//No more bits that can be used

public val ENTITY_MASK: ULong = 0x00FFFFFFFFFFFFFFuL
public val TRAIT_MASK: ULong = 0xFFFFFFFF00000000uL
public val COMP_MASK: ULong = 0x00000000FFFFFFFFuL
