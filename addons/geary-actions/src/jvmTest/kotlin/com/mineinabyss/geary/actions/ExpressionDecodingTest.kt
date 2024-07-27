package com.mineinabyss.geary.actions

import com.charleskorn.kaml.Yaml
import com.mineinabyss.geary.actions.expressions.Expression
import com.mineinabyss.geary.serialization.formats.YamlFormat
import com.mineinabyss.geary.serialization.serializableComponents
import io.kotest.matchers.shouldBe
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
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

    @org.junit.jupiter.api.Test
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
            age = Expression.Evaluate("test"),
            regular = "{{ asdf }}"
        )
    }
}
