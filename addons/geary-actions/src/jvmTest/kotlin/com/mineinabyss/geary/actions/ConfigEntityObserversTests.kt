package com.mineinabyss.geary.actions

import com.mineinabyss.geary.actions.event_binds.EntityObservers
import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.modules.observeWithData
import com.mineinabyss.geary.serialization.SerializableComponents
import com.mineinabyss.geary.serialization.dsl.withCommonComponentNames
import com.mineinabyss.geary.serialization.formats.YamlFormat
import com.mineinabyss.geary.serialization.serialization
import com.mineinabyss.geary.serialization.serializers.GearyEntitySerializer
import io.kotest.matchers.shouldBe
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import org.junit.jupiter.api.Test
import test.GearyTest

class ConfigEntityObserversTests : GearyTest() {
    @Serializable
    @SerialName("geary:print")
    data class Print(val string: String)

    @Serializable
    @SerialName("geary:my_comp")
    class MyComp()

    override fun setupGeary() = geary(TestEngineModule) {
        install(GearyActions)

        serialization {
            withCommonComponentNames()

            components {
                component(String.serializer())
                component(Print.serializer())
                component(EntityObservers.serializer())
                component(MyComp.serializer())
            }
        }
    }

    @Test
    fun `should correctly add temporary and persisting components with CopyToInstances`() {
        // arrange
        val entityDef = """
        geary:observe:
          geary:onSet:
           # TODO involving: [ geary:myComp ]
              - geary:print:
                  string: "Hello World"
        """.trimIndent()

        val format = YamlFormat(getAddon(SerializableComponents).serializers.module)
        val entity = format.decodeFromString(GearyEntitySerializer(this), entityDef)
        val printed = mutableListOf<String>()
        observeWithData<Print>().exec { printed += event.string }

        // act
        entity.set(MyComp())

        // assert
        printed shouldBe listOf("Hello World")
    }
}
