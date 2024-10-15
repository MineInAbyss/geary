package com.mineinabyss.geary.helpers

import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.modules.Geary
import kotlin.reflect.KClass

fun EntityId.readableString(world: Geary): String = buildString {
    val id = this@readableString
    if (id.hasRole(RELATION)) {
        append(id.toRelation().toString())
        return@buildString
    }
    if (id.hasRole(RELATION)) append("R") else append('-')
    if (id.hasRole(HOLDS_DATA)) append("D") else append('-')
    append(" ")
    val componentName = (id.getComponentInfo(world)?.kClass as? KClass<*>)?.simpleName
    if (componentName == null) append(id and ENTITY_MASK)
    else append(componentName)
}
