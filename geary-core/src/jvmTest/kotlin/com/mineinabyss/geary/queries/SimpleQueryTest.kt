package com.mineinabyss.geary.queries

import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.modules.dsl.track
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.accessors.Pointer
import com.mineinabyss.geary.systems.query.Query
import com.mineinabyss.geary.systems.query.system
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SimpleQueryTest : GearyTest() {
    class MyQuery : Query() {
        val int by target.get<Int>()
    }

    @Test
    fun `simple query`() {
        val query = geary.queryManager.trackQuery(MyQuery())
        repeat(10) {
            entity {
                set(1)
            }
            entity {
                set("Not this!")
            }
        }

        var count = 0
        query.forEach {
            int shouldBe 1
            count++
        }
        count shouldBe 10
    }
}
