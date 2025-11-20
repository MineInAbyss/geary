package com.mineinabyss.geary.serialization

import com.mineinabyss.geary.addons.dsl.GearyDSL
import com.mineinabyss.geary.modules.GearySetup

//data class SerializableComponentsBuilder(
//    val world: Geary,
//    val serializers: ComponentSerializersBuilder = ComponentSerializersBuilder(),
//    val formats: FormatsBuilder = FormatsBuilder(),
//    var overridePersists: ComponentId? = null,
//    var addGearyEntitySerializer: Boolean = true,
//) {
//    /** Adds a [SerializersModule] for polymorphic serialization of [Component]s within the ECS. */
//    inline fun components(crossinline init: PolymorphicModuleBuilder<Component>.() -> Unit) {
//        module { polymorphic(Component::class) { init() } }
//    }
//
//    fun format(ext: String, format: (SerializersModule) -> Format) {
//        formats.register(ext, format)
//    }
//
//    /**
//     * Adds a serializable component and registers it with Geary to allow finding the appropriate class via
//     * component serial name.
//     */
//    inline fun <reified T : Component> PolymorphicModuleBuilder<T>.component(serializer: KSerializer<T>) {
//        component(T::class, serializer)
//    }
//
//    /**
//     * Adds a serializable component and registers it with Geary to allow finding the appropriate class via
//     * component serial name.
//     */
//    @OptIn(InternalSerializationApi::class)
//    fun <T : Component> PolymorphicModuleBuilder<T>.component(
//        subclass: KClass<T>,
//        serializer: KSerializer<T> = subclass.serializerOrNull()
//            ?: error("No serializer found for $subclass while registering serializable component"),
//    ) {
//        val serialName = serializer.descriptor.serialName
//        if (serializers.serialNameToClass.containsKey(serialName)) {
//            error("A component with serial name $serialName is already registered")
//        }
//        serializers.serialNameToClass[serialName] = subclass
//        subclass(subclass, serializer)
//    }
//
//    inline fun <reified T : Any> namedComponent(name: String) {
//        serializers.serialNameToClass[name] = T::class
//    }
//
//    /** Adds a [SerializersModule] to be used for polymorphic serialization within the ECS. */
//    inline fun module(init: SerializersModuleBuilder.() -> Unit) {
//        serializers.modules += SerializersModule { init() }
//    }
//
//    fun build(): SerializableComponentsModule {
//        module {
//            contextual<Geary>(GearyWorldProvider(world))
//            contextual<ComponentId>(ComponentIdSerializer(serializers.build(), world))
//        }
//        val serializers = serializers.build()
//        if (addGearyEntitySerializer) {
//            components { component(GearyEntitySerializer()) }
//        }
//        return SerializableComponentsModule(
//            serializers = serializers,
//            formats = formats.build(serializers),
//            persists = overridePersists ?: world.componentId<Persists>()
//        )
//    }
//}


@GearyDSL
fun GearySetup.serialization(configure: SerializableComponentsModule.() -> Unit) =
    install(SerializableComponents).apply(configure)
