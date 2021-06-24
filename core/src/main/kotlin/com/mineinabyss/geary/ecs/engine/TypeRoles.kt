@file:OptIn(ExperimentalUnsignedTypes::class)

package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.relations.Relation

//can't make const because of the shl
public val INSTANCEOF: ULong = 1uL shl 63
public val CHILDOF: ULong = 1uL shl 62
public val RELATION: ULong = 1uL shl 61
public val HOLDS_DATA: ULong = 1uL shl 60
//5
//6
//7
//8
//No more bits that can be used

public const val ENTITY_MASK: ULong = 0x00FFFFFFFFFFFFFFuL
public const val RELATION_PARENT_MASK: ULong = 0x00FFFFFF00000000uL
public const val RELATION_COMPONENT_MASK: ULong = 0xFF000000FFFFFFFFuL

public fun GearyComponentId.isInstance(): Boolean = this or INSTANCEOF != 0uL
public fun GearyComponentId.isChild(): Boolean = this or CHILDOF != 0uL
public fun GearyComponentId.isRelation(): Boolean = this or RELATION != 0uL
public fun GearyComponentId.holdsData(): Boolean = this or HOLDS_DATA != 0uL

public fun GearyComponentId.toRelation(): Relation? =
    Relation(this).takeIf { isRelation() }
