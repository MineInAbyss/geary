package com.mineinabyss.geary.queries

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.geary.test.GearyTest
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test

class QueryForEachMutatingTest : GearyTest() {
    @BeforeEach
    fun setup() {
        resetEngine()
        repeat(10) {
            entity { set<Int>(it) }
        }
    }

    @Test
    fun `forEach should fail when modifying unsafeEntity's archetype`() {
        val query = queryManager.trackQuery(query<Int>())
        shouldThrowAny {
            @OptIn(UnsafeAccessors::class)
            query.forEach {
                it.unsafeEntity.toGeary().remove<Int>()
            }
        }
    }

    @Test
    fun `forEachEntity should allow modifying archetypes while iterating`() {
        val query = queryManager.trackQuery(query<Int>())
        val postQuery = queryManager.trackQuery(query<Long>())

        @OptIn(UnsafeAccessors::class)
        query.forEachMutating { entity, (integer) ->
            entity.remove<Int>()
            entity.set<Long>(integer.toLong())
        }

        query.entities().shouldBeEmpty()
        postQuery.entities().shouldHaveSize(10)
    }
}
