package com.mineinabyss.geary.queries.accessors

import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.builders.cache
import com.mineinabyss.geary.systems.query.Query
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class MappedAccessorTests : GearyTest() {
    private class Marker

    private fun mappedQuery() = geary.cache(object : Query() {
        val mapped by get<Int>().map { it.toString() }
    })

    private fun defaultingQuery() = geary.cache(object : Query() {
        val default by get<String>().orDefault { "empty!" }
        override fun ensure() = this { has<Marker>() }
    })

    @Test
    fun `should correctly get mapped accessors`() {
        entity {
            set(1)
        }
        mappedQuery().forEach {
            it.mapped shouldBe "1"
        }
    }

    @Test
    fun `should correctly get default accessors`() {
        entity {
            set("Hello")
            add<Marker>()
        }
        entity {
            add<Marker>()
        }
        defaultingQuery().map { it.default }.shouldContainExactly("Hello", "empty!")
    }
}
