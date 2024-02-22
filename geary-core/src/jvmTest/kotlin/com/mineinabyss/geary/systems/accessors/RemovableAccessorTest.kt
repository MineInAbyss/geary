package com.mineinabyss.geary.systems.accessors

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.helpers.Comp1
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.cachedQuery
import com.mineinabyss.geary.systems.query.Query
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class RemovableAccessorTest : GearyTest() {
    private fun createRemovableQuery() = geary.cachedQuery(object : Query() {
        var data by target.get<Comp1>().removable()
    })


    @OptIn(UnsafeAccessors::class)
    @Test
    fun `should allow removing component via removable accessor`() {
        resetEngine()
        entity {
            set(Comp1(1))
        }
        var count = 0

        createRemovableQuery().forEach {
            data shouldBe Comp1(1)
            data = null
            data shouldBe null
            target.entity.has<Comp1>() shouldBe false
            count++
        }
        count shouldBe 1
    }
}
