package com.mineinabyss.geary.actions

import com.charleskorn.kaml.Yaml
import com.mineinabyss.geary.actions.expressions.Expression
import com.mineinabyss.geary.actions.expressions.FunctionExpression
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.serialization.dsl.serialization
import com.mineinabyss.geary.serialization.formats.YamlFormat
import com.mineinabyss.idofront.di.DI
import io.kotest.matchers.shouldBe
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import org.junit.jupiter.api.Test

class ExpressionDecodingTest {
    @Serializable
    data class TestData(
        val name: Expression<String>,
        val age: Expression<Int>,
        val regular: String,
    )

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
        Yaml.default.decodeFromString(TestData.serializer(), input) shouldBe TestData(
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
        DI.clear()
        geary(TestEngineModule){
            serialization {
                components {
                    component(TestFunction.serializer())
                }
                format("yml", ::YamlFormat)
            }

        }

        geary.pipeline.runStartupTasks()
        val input = "'{{ entity.geary:testFunction{ string: test } }}'"
        val expr = Yaml.default.decodeFromString(Expression.Serializer(String.serializer()), input)
        expr.evaluate(ActionGroupContext(entity = entity())) shouldBe "test"
    }
}
