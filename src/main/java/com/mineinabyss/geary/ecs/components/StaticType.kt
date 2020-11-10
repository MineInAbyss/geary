package com.mineinabyss.geary.ecs.components

import com.mineinabyss.geary.ecs.GearyComponent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Serializable
@SerialName("mobzy:statictype")
class StaticType(
        val name: String
) : GearyComponent() {
    // TODO val type by lazy {  }
    //TODO types param to work with other things
    fun getComponent(kClass: KClass<out GearyComponent>/*types: GearyEntityTypes<*>*/): GearyComponent? =
            TODO()
//            LootyTypes[name].staticComponentMap[kClass]
}
