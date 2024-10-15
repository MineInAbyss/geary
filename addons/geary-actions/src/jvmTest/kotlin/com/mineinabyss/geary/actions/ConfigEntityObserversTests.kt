package com.mineinabyss.geary.actions

import com.mineinabyss.geary.actions.event_binds.EntityObservers
import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.serialization.dsl.serialization
import com.mineinabyss.geary.serialization.dsl.withCommonComponentNames
import com.mineinabyss.geary.serialization.formats.YamlFormat
import com.mineinabyss.geary.serialization.serializableComponents
import com.mineinabyss.geary.serialization.serialization
import com.mineinabyss.geary.serialization.serializers.GearyEntitySerializer
import com.mineinabyss.geary.systems.builders.observeWithData
import com.mineinabyss.idofront.di.DI
import io.kotest.matchers.shouldBe
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ConfigEntityObserversTests {
    @Serializable
    @SerialName("geary:print")
    data class Print(val string: String)

    @Serializable
    @SerialName("geary:my_comp")
    class MyComp()

    @BeforeEach
    fun createEngine() {
        DI.clear()
        geary(TestEngineModule) {
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
        }.start()
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

        val format = YamlFormat(serializableComponents.serializers.module)
        val entity = format.decodeFromString(GearyEntitySerializer, entityDef)
        val printed = mutableListOf<String>()
        geary.observeWithData<Print>().exec { printed += event.string }

        // act
        entity.set(MyComp())

        // assert
        printed shouldBe listOf("Hello World")
    }
}
