package com.mineinabyss.geary.queries.accessors

import com.mineinabyss.geary.helpers.Comp1
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.test.GearyTest
import com.mineinabyss.geary.systems.query.Query
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.geary.systems.query.row
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class AccessorDataModificationTests : GearyTest() {
    private fun registerQuery() = cache(query<Comp1>())

    @Test
    fun `should allow data modify via accessor`() {
        resetEngine()
        entity {
            set(Comp1(1))
        }

        var count = 0
        registerQuery().forEach { query ->
            query.comp1 shouldBe Comp1(1)
            query.accessor1[row] = Comp1(10)
            query.comp1 shouldBe Comp1(10)
            count++
        }
        count shouldBe 1
    }
}
