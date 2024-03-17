package com.mineinabyss.geary.queries

import com.mineinabyss.geary.annotations.optin.ExperimentalGearyApi
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.query.Query
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeAll
import kotlin.test.Test

@OptIn(ExperimentalGearyApi::class)
class SimpleQueryTest : GearyTest() {
    class MyQuery : Query() {
        val int by get<Int>()
    }

    @BeforeAll
    fun ensureTestEntitiesExist() {
        repeat(10) {
            entity {
                set(it)
            }
            entity {
                set("Not this!")
            }
        }
    }

    @Test
    fun `forEach should allow reading data`() {
        val query = geary.queryManager.trackQuery(MyQuery())

        val nums = mutableListOf<Int>()
        query.forEach {
            nums.add(int)
        }
        nums.sorted() shouldBe (0..9).toList()
    }

    @Test
    fun `first should correctly return first matched entity`() {
        val query = geary.queryManager.trackQuery(MyQuery())

        query.find({ int }, { int == 5 }) shouldBe 5
    }

    @Test
    fun `any should correctly check for matches if matched`() {
        val query = geary.queryManager.trackQuery(MyQuery())

        query.any { int == 5 } shouldBe true
        query.any { int == 100 } shouldBe false
    }

    @Test
    fun `should be able to collect query as sequence`() {
        val query = geary.queryManager.trackQuery(MyQuery())

        query.collect {
            filter { it.int % 2 == 0 }.map { it.int.toString() }.toList()
        } shouldBe (0..9 step 2).map { it.toString() }
    }

    @Test
    fun `should allow collecting fancier sequences`() {
        val query = geary.queryManager.trackQuery(MyQuery())

        query.collect {
            filter { it.int % 2 == 0 }.map { it.int }.sortedByDescending { it }.take(3).toList()
        } shouldBe listOf(8, 6, 4)
    }

    @Test
    fun `should not allow working on sequence outside collect block`() {
        val query = geary.queryManager.trackQuery(MyQuery())

        shouldThrow<IllegalStateException> {
            query.collect {
                filter { it.int % 2 == 0 }
            }.map { it.int.toString() }.toList()
        }
    }
}
