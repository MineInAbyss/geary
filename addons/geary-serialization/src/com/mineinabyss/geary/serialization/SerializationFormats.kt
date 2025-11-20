package com.mineinabyss.geary.serialization

import com.mineinabyss.geary.serialization.formats.Format
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.modules.plus

class SerializationFormats() {
    var module: SerializersModule = EmptySerializersModule()
        private set
    private val formatsBuilders: MutableMap<String, (SerializersModule) -> Format> = mutableMapOf()
    private val formats: MutableMap<String, Format> = mutableMapOf()

    private var binaryFormatBuilder: (SerializersModule) -> BinaryFormat = { Cbor { serializersModule = it } }
    private var _binaryFormat: BinaryFormat? = null

    val binaryFormat: BinaryFormat get() = _binaryFormat ?: binaryFormatBuilder(module)

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