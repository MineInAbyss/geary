package com.mineinabyss.geary.serialization.helpers

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.modules.WorldScoped
import com.mineinabyss.geary.serialization.SerializableComponents

/**
 * Gets the id of a component by its serial name.
 * Throws an error if the component name does not exist.
 */
fun WorldScoped.componentId(serialName: String): ComponentId =
    componentId(getAddon(SerializableComponents).serializers.getClassFor(serialName))
