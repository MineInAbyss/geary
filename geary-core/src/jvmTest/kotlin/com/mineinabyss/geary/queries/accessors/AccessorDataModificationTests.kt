package com.mineinabyss.geary.queries.accessors

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.helpers.Comp1
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.builders.cache
import com.mineinabyss.geary.systems.query.Query
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class AccessorDataModificationTests : GearyTest() {
    private fun registerQuery() = geary.cache(object : Query() {
        var data by get<Comp1>()
    })

    @Test
    fun `should allow data modify via accessor`() {
        resetEngine()
        entity {
            set(Comp1(1))
        }

        var count = 0
        registerQuery().forEach {
            data shouldBe Comp1(1)
            data = Comp1(10)
            data shouldBe Comp1(10)
            count++
        }
        count shouldBe 1
    }
}
