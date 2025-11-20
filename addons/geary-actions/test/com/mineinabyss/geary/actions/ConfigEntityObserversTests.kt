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
import com.mineinabyss.geary.test.GearyTest
import io.kotest.matchers.shouldBe
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import org.junit.jupiter.api.Test

class ConfigEntityObserversTests : GearyTest() {
    @Serializable
    @SerialName("geary:print")
    data class Print(val string: String): Action {
        override fun ActionGroupContext.execute(): Any? {
            entity?.emit<Print>(Print(string))
            return null
        }

    }

    @Serializable
    @SerialName("geary:my_comp")
    class MyComp()

    override fun setupGeary() = geary(TestEngineModule) {
        install(GearyActions)

        serialization {
            withCommonComponentNames()

            registerComponentSerializers(
                String.serializer(),
                Print.serializer(),
                EntityObservers.serializer(),
                MyComp.serializer(),
            )
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

        val format = YamlFormat(getAddon(SerializableComponents).formats.module)
        val entity = format.decodeFromString(GearyEntitySerializer(), entityDef)
        val printed = mutableListOf<String>()
        observeWithData<Print>().exec { printed += event.string }

        // act
        entity.set(MyComp())

        // assert
        printed shouldBe listOf("Hello World")
    }
}
