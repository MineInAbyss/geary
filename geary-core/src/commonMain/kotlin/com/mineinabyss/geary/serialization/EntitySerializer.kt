package com.mineinabyss.geary.serialization

import com.mineinabyss.geary.components.EntityName
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.parent
import com.mineinabyss.geary.helpers.toGeary

//TODO this should be handled within a serializer of sorts for GearyEntity
public fun Entity.parseEntity(expression: String): Entity {
    return when {
        expression.startsWith("parent") -> {
            val parent = (parent ?: error("Failed to read expression, entity had no parent: $expression"))
            if (expression.startsWith("parent."))
                parent.parseEntity(expression.removePrefix("parent."))
            else parent
        }

        expression.startsWith("child") -> {
            val childName = expression.substringAfter('(').substringBefore(')')
            val child = (children.find { it.get<EntityName>()?.name == childName }
                ?: error("No child named $childName found: $expression"))
            val childExpression = expression.substringAfter("].", missingDelimiterValue = "")

            if (childExpression != "")
                child.parseEntity(childExpression)
            else child
        }

        expression.contains(':') -> componentId(expression).toGeary()
        else -> error("Malformed expression for getting entity: $expression")
    }
}
