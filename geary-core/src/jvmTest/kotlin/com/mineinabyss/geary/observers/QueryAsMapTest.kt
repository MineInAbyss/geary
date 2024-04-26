package com.mineinabyss.geary.observers

import com.mineinabyss.geary.events.queries.cacheAssociatedBy
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.query.query
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class QueryAsMapTest : GearyTest() {
    @Test
    fun `should correctly track entity in map`() {
        val map = geary.cacheAssociatedBy(query<String>()) { (string) -> string }

        entity {
            set("Hello world")
            map["Hello world"] shouldBe this
            remove<String>()
            map["Hello world"] shouldBe null
            set("Hello world")
            removeEntity()
            map["Hello world"] shouldBe null
        }
    }
}
