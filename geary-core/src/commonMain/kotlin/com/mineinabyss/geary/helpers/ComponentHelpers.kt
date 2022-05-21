package com.mineinabyss.geary.helpers

import com.mineinabyss.geary.context.globalContext
import com.mineinabyss.geary.datatypes.*
import kotlin.reflect.KClass

/** Neatly lists all the components on this entity. */
public fun GearyEntity.listComponents(): String {
    return """Instance:
        ${getInstanceComponents().joinToString("\n") { "${it::class.simpleName}: $it" }}
        
        Persisting:
        ${getAllPersisting().joinToString("\n") { "${globalContext.serializers.getSerialNameFor(it::class)}: $it" }}
        """.trimIndent()
}

public fun GearyEntityId.readableString(): String = buildString {
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
