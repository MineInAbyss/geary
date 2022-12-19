package com.mineinabyss.geary.helpers

import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.datatypes.*
import kotlin.reflect.KClass

/** Neatly lists all the components on this entity. */
fun Entity.listComponents(): String {
    return """Instance:
        ${getAllNotPersisting().joinToString("\n") { "${it::class.simpleName}: $it" }}
        
        Persisting:
        ${getAllPersisting().joinToString("\n") { "${geary.serializers.getSerialNameFor(it::class)}: $it" }}
        """.trimIndent()
}

fun EntityId.readableString(): String = buildString {
    val id = this@readableString
    if(id.hasRole(RELATION)) {
        append(id.toRelation().toString())
        return@buildString
    }
    if(id.hasRole(RELATION)) append("R") else append('-')
    if(id.hasRole(HOLDS_DATA)) append("D") else append('-')
    append(" ")
    val componentName = (id.getComponentInfo()?.kClass as? KClass<*>)?.simpleName
    if(componentName == null) append(id and ENTITY_MASK)
    else append(componentName)
}
