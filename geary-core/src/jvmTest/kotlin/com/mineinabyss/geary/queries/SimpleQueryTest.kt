package com.mineinabyss.geary.queries

import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.query.Query
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SimpleQueryTest : GearyTest() {
    class MyQuery : Query() {
        val int by get<Int>()
    }

    @Test
    fun `simple query`() {
        val query = geary.queryManager.trackQuery(MyQuery())
        repeat(10) {
            entity {
                set(it)
            }
            entity {
                set("Not this!")
            }
        }

        val nums = mutableListOf<Int>()
        query.forEach {
            nums.add(int)
        }
        nums.sorted() shouldBe (0..9).toList()
    }
}
