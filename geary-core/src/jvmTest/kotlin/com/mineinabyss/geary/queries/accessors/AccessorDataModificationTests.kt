package com.mineinabyss.geary.queries.accessors

import com.mineinabyss.geary.helpers.Comp1
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.test.GearyTest
import com.mineinabyss.geary.systems.query.Query
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class AccessorDataModificationTests : GearyTest() {
    private fun registerQuery() = cache(object : Query(this) {
        var data by get<Comp1>()
    })

    @Test
    fun `should allow data modify via accessor`() {
        resetEngine()
        entity {
            set(Comp1(1))
        }

        var count = 0
        registerQuery().forEach { q ->
            q.data shouldBe Comp1(1)
            q.data = Comp1(10)
            q.data shouldBe Comp1(10)
            count++
        }
        count shouldBe 1
    }
}
