package com.mineinabyss.geary.ecs.types

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.components.StaticType
import com.mineinabyss.geary.ecs.engine.Engine
import com.mineinabyss.geary.ecs.serialization.Formats
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.SetSerializer

@Serializable
abstract class GearyEntityType {
    @SerialName("name")
    private val _name: String? = null
    private val staticComponents: Set<GearyComponent> = mutableSetOf()
    private val components: Set<GearyComponent> = setOf()

    /** Resulting set will be added to the list of components, but won't be serialized. */
    open fun MutableSet<GearyComponent>.additionalComponents() {}

    /** Resulting set will be added to the list of static components, but won't be serialized. */
    open fun MutableSet<GearyComponent>.additionalStaticComponents() {}

    protected abstract val types: GearyEntityTypes<*>

    val name by lazy { _name ?: types.getNameForTemplate(this) }

    //TODO this is the safest and cleanest way to deepcopy. Check how this performs vs deepcopy's reflection method.
    private val serializedComponents: String by lazy {
        Formats.yamlFormat.encodeToString(
                componentSerializer,
                components.plus(mutableSetOf<GearyComponent>().apply { additionalComponents() })
        )
    }

    val staticComponentMap by lazy {
        staticComponents
                .plus(mutableSetOf<GearyComponent>().apply { additionalStaticComponents() })
                .associateBy { it::class }
    }

    fun instantiateComponents(existingComponents: Set<GearyComponent> = emptySet()): Set<GearyComponent> =
            Formats.yamlFormat.decodeFromString(componentSerializer, serializedComponents).apply {
                forEach { it.persist = true }
            } + existingComponents + staticComponents + StaticType(types.plugin.name, name)
    // + staticComponents TODO incorporate into the types system

    /** Creates a new instance of this type's defined entity, which is registered with the [Engine] */
    abstract fun instantiate(): GearyEntity

    inline fun <reified T : GearyComponent> get(): T? = staticComponentMap[T::class] as? T

    inline fun <reified T : GearyComponent> has() = staticComponentMap.containsKey(T::class)

    companion object {
        private val componentSerializer = SetSerializer(PolymorphicSerializer(GearyComponent::class))
    }
}
