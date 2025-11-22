package com.mineinabyss.geary.serialization

import com.mineinabyss.geary.datatypes.Component
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.serialization.formats.Format
import com.mineinabyss.geary.serialization.serializers.ComponentIdSerializer
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.modules.plus
import kotlin.reflect.KClass

class SerializationFormats(
    val serializers: ComponentSerializers,
    val world: Geary,
) {
    var module: SerializersModule = SerializersModule {
        contextual(ComponentId::class, ComponentIdSerializer(serializers, world))
        contextual(Geary::class, GearyWorldProvider(world))
    }
        private set

    private val formatsBuilders: MutableMap<String, (SerializersModule) -> Format> = mutableMapOf()
    private val formats: MutableMap<String, Format> = mutableMapOf()

    private var binaryFormatBuilder: (SerializersModule) -> BinaryFormat = { Cbor { serializersModule = it } }
    private var _binaryFormat: BinaryFormat? = null

    val binaryFormat: BinaryFormat get() = _binaryFormat ?: binaryFormatBuilder(module)

    fun <T : Component> getSerializerFor(
        key: String,
        baseClass: KClass<in T>,
    ): DeserializationStrategy<T>? =
        module.getPolymorphic(baseClass = baseClass, serializedClassName = key)

    fun <T : Component> getSerializerFor(kClass: KClass<in T>): DeserializationStrategy<T>? {
        val serialName = serializers.getSerialNameFor(kClass) ?: return null

        return getSerializerFor(serialName, Component::class)
    }

    fun addModule(module: SerializersModule) {
        invalidateFormats()
        this.module = this.module.plus(module)
    }

    fun addModule(block: SerializersModuleBuilder.() -> Unit) {
        invalidateFormats()
        this.module = this.module.plus(SerializersModule { block() })
    }

    fun getFormat(ext: String): Format {
        return formats.getOrPut(ext) {
            formatsBuilders[ext]?.invoke(module) ?: error("No format registered for extension $ext")
        }
    }

    fun registerFormat(key: String, format: (SerializersModule) -> Format) {
        _binaryFormat = null
        formatsBuilders[key] = format
    }

    fun setBinaryFormat(block: (SerializersModule) -> BinaryFormat) {
        binaryFormatBuilder = block
    }

    private fun invalidateFormats() {
        formats.clear()
    }
}