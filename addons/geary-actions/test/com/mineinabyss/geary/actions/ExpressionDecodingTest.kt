package com.mineinabyss.geary.actions

import com.mineinabyss.geary.actions.expressions.Expression
import com.mineinabyss.geary.actions.expressions.FunctionExpression
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.serialization.SerializableComponents
import com.mineinabyss.geary.serialization.formats.YamlFormat
import com.mineinabyss.geary.serialization.serialization
import com.mineinabyss.geary.test.GearyTest
import io.kotest.matchers.shouldBe
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import org.junit.jupiter.api.Test

class ExpressionDecodingTest : GearyTest() {
    @Serializable
    data class TestData(
        val name: Expression<String>,
        val age: Expression<Int>,
        val regular: String,
    )

    override fun setupGeary() = geary(TestEngineModule) {
        serialization {
            registerComponentSerializers(
                TestFunction.serializer()
            )
            format("yml", ::YamlFormat)
        }
    }

    val format get() = getAddon(SerializableComponents).formats["yml"] as YamlFormat
//    @org.junit.jupiter.api.Test
//    fun `should correctly decode json`() {
//        val input = """
//        {
//            "age": "{{ test }}",
//            "name": "variable",
//            "regular": "{{ asdf }}"
//        }
//        """.trimIndent()
//        Json.decodeFromString<TestData>(input) shouldBe TestData(
//            name = Expression.Fixed("variable"),
//            age = Expression.Evaluate("test"),
//            regular = "{{ asdf }}"
//        )
//    }

    @Test
    fun `should correctly decode yaml`() {
        val input = """
        {
            "age": "{{ test }}",
            "name": "variable",
            "regular": "{{ asdf }}"
        }
        """.trimIndent()
        format.decodeFromString(TestData.serializer(), input) shouldBe TestData(
            name = Expression.Fixed("variable"),
            age = Expression.Variable("test"),
            regular = "{{ asdf }}"
        )
    }

    @Serializable
    @SerialName("geary:test_function")
    class TestFunction(val string: String) : FunctionExpression<GearyEntity, String> {
        override fun ActionGroupContext.map(input: GearyEntity): String {
            return string
        }
    }

    @Test
    fun shouldCorrectlyParseExpressionFunctions() {
        val input = "'{{ entity.geary:testFunction{ string: test } }}'"
        val expr = format.decodeFromString(Expression.Serializer(String.serializer()), input)
        expr.evaluate(ActionGroupContext(entity = entity())) shouldBe "test"
    }
}
