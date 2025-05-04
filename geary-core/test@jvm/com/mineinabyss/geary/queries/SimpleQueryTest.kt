package com.mineinabyss.geary.queries

import com.mineinabyss.geary.annotations.optin.ExperimentalGearyApi
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.test.GearyTest
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.geary.systems.query.toList
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeAll
import kotlin.test.Test

@OptIn(ExperimentalGearyApi::class)
class SimpleQueryTest : GearyTest() {
    val myQuery = query<Int>()

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
        val query = queryManager.trackQuery(myQuery)

        val nums = mutableListOf<Int>()
        query.forEach { (int) ->
            nums.add(int)
        }
        nums.sorted() shouldBe (0..9).toList()
    }

    @Test
    fun `entities should return matched entities correctly`() {
        val query = queryManager.trackQuery(myQuery)

        val nums = mutableListOf<Int>()
        query.entities().forEach {
            nums.add(it.get<Int>()!!)
        }

        nums.sorted() shouldBe (0..9).toList()
    }

    @Test
    fun `first should correctly return first matched entity`() {
        val query = queryManager.trackQuery(myQuery)

        query.find({ it.comp1 }, { it.comp1 == 5 }) shouldBe 5
    }

    @Test
    fun `any should correctly check for matches if matched`() {
        val query = queryManager.trackQuery(myQuery)

        query.any { it.comp1 == 5 } shouldBe true
        query.any { it.comp1 == 100 } shouldBe false
    }

    @Test
    fun `should be able to collect query as sequence`() {
        val query = queryManager.trackQuery(myQuery)

        query.collect {
            filter { it.comp1 % 2 == 0 }.map { it.comp1.toString() }.toList()
        } shouldBe (0..9 step 2).map { it.toString() }
    }

    @Test
    fun `should allow collecting fancier sequences`() {
        val query = queryManager.trackQuery(myQuery)

        query.collect {
            filter { it.comp1 % 2 == 0 }.map { it.comp1 }.sortedByDescending { it }.take(3).toList()
        } shouldBe listOf(8, 6, 4)
    }

    @Test
    fun `should not allow working on sequence outside collect block`() {
        val query = queryManager.trackQuery(myQuery)

        shouldThrow<IllegalStateException> {
            query.collect {
                filter { it.comp1 % 2 == 0 }
            }.map { it.comp1.toString() }.toList()
        }
    }

    @Test
    fun `should allow querying nullable types as optional in shorthand queries`() {
        entity {
            set<Int>(1)
            set("Both")
        }
        entity {
            set("Only string")
        }
        val query = queryManager.trackQuery(query<Int, String?>())
        val matched = query.toList()
        assertSoftly(matched) {
            shouldContainAll((0..9 step 2).map { it to null })
            shouldContain(1 to "Both")
            withClue("Should not include anything else") {
                shouldHaveSize(11)
            }
        }
    }
}
