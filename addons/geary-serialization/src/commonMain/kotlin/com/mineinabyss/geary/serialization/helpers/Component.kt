package com.mineinabyss.geary.serialization.helpers

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.serialization.serializableComponents

/**
 * Gets the id of a component by its serial name.
 * Throws an error if the component name does not exist.
 */
fun componentId(serialName: String): ComponentId =
    componentId(serializableComponents.serializers.getClassFor(serialName))
