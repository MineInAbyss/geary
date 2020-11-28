package com.mineinabyss.geary.ecs.types

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.components.StaticType
import com.mineinabyss.geary.ecs.engine.ComponentClass
import com.mineinabyss.geary.ecs.engine.Engine
import com.mineinabyss.geary.ecs.serialization.Formats
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.builtins.SetSerializer

@Serializable
public abstract class GearyEntityType {
    //TODO shouldn't be public
    public val staticComponents: Set<GearyComponent> = mutableSetOf()
    public val components: Set<GearyComponent> = setOf()

    /** Resulting set will be added to the list of components, but won't be serialized. */
    protected open fun MutableSet<GearyComponent>.additionalComponents() {}

    /** Resulting set will be added to the list of static components, but won't be serialized. */
    protected open fun MutableSet<GearyComponent>.additionalStaticComponents() {}

    protected abstract val types: GearyEntityTypes<*>

    @Transient
    public lateinit var name: String
        internal set

    //TODO this is the safest and cleanest way to deepcopy. Check how this performs vs deepcopy's reflection method.
    private val serializedComponents: String by lazy {
        Formats.yamlFormat.encodeToString(
                componentSerializer,
                //FIXME this plus here will add persistent components when we might not want that
                components.plus(mutableSetOf<GearyComponent>().apply { additionalComponents() })
        )
    }

    public val staticComponentMap: Map<ComponentClass, GearyComponent> by lazy {
        staticComponents
                .plus(mutableSetOf<GearyComponent>().apply { additionalStaticComponents() })
                .associateBy { it::class }
    }

    public fun instantiateComponents(existingComponents: Set<GearyComponent> = emptySet()): Set<GearyComponent> =
            //TODO not sure if all non-static components should be serialized (marked as persistent.
            // There is probably a use case for components that are always added on entity load, but the serialized
            // version is only stored within the static type.
            //The quick fix for now is to make `persistent` a serialized value so each component can decide
            // whether it should be persistent for itself. This uses more data.
            Formats.yamlFormat.decodeFromString(componentSerializer, serializedComponents)/*.onEach { it.persist = true }*/ +
                    existingComponents +
                    staticComponents +
                    StaticType(types.plugin.name, name)
    // + staticComponents TODO incorporate into the types system

    /** Creates a new instance of this type's defined entity, which is registered with the [Engine] */
    public abstract fun instantiate(): GearyEntity

    public inline fun <reified T : GearyComponent> get(): T? = staticComponentMap[T::class] as? T

    public inline fun <reified T : GearyComponent> has(): Boolean = staticComponentMap.containsKey(T::class)

    internal companion object {
        private val componentSerializer = SetSerializer(PolymorphicSerializer(GearyComponent::class))
    }
}
