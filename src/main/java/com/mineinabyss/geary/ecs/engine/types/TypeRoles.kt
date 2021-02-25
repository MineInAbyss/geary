@file:OptIn(ExperimentalUnsignedTypes::class)

package com.mineinabyss.geary.ecs.engine.types

public val INSTANCEOF: ULong = 1uL shl 63
public val CHILDOF: ULong = 1uL shl 62
public val TRAIT: ULong = 1uL shl 61

public val ID_MASK: ULong = (0b11111111uL shl 56).inv()
