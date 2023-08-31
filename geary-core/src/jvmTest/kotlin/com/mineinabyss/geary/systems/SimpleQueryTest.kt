package com.mineinabyss.geary.systems

import com.mineinabyss.geary.datatypes.GearyRecord
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.systems.query.Query
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SimpleQueryTest : GearyTest() {
    object MyQuery : Query() {
        val GearyRecord.int by get<Int>()
    }

    @Test
    fun `simple query`() {
        repeat(10) {
            entity {
                set(1)
            }
            entity {
                set("Not this!")
            }
        }

        var count = 0
        MyQuery.run {
            fastForEach {
                it.int shouldBe 1
                count++
            }
        }
        count shouldBe 10
    }
}
