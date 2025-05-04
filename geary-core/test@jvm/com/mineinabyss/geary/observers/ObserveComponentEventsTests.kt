package com.mineinabyss.geary.observers

import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.test.GearyTest
import com.mineinabyss.geary.modules.observe
import com.mineinabyss.geary.observers.events.OnAdd
import com.mineinabyss.geary.observers.events.OnRemove
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.systems.query.query
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class ObserveComponentEventsTests : GearyTest() {
    @BeforeEach
    fun reset() {
        resetEngine()
    }

    @Nested
    inner class OnSetTests {
        @Test
        fun `should correctly observe OnSet events involving single component`() {
            var called = 0
            observe<OnSet>().involving<Int>().exec { called += 1 }

            val entity = entity()
            called shouldBe 0
            entity.set(1.0)
            called shouldBe 0
            entity.set(1)
            called shouldBe 1
        }

        @Test
        fun `should correctly observe OnSet events involving multiple components`() {
            var called = 0

            observe<OnSet>().involving(query<String, Int, Double>()).exec { called++ }

            entity {
                set("")
                set(1)
                called shouldBe 0
                set(1.0)
                called shouldBe 1
                set(1f)
                called shouldBe 1
                set("a")
                called shouldBe 2
            }
        }
    }

    @Nested
    inner class OnAddTests {
        @Test
        fun `should observe add event when component added or set`() {
            var called = 0
            observe<OnAdd>().involving<Int>().exec {
                called += 1
            }

            val entity = entity()

            called shouldBe 0
            entity.add<Int>()
            called shouldBe 1
        }
    }

    @Nested
    inner class OnRemoveTests {
        @Test
        fun `should fire remove listener and have access to removed component data when component removed`() {
            // arrange
            var called = 0
            observe<OnRemove>().involving<String>().exec(query<String>()) { (string) ->
                string shouldBe "data"
                called++
            }
            val entity = entity {
                set("data")
            }

            // act
            entity.remove<String>()

            // assert
            called shouldBe 1
        }

        @Test
        fun `should not fire remove listener when other component removed`() {
            // arrange
            var called = 0


            observe<OnRemove>().involving<String>().exec {
                called++
            }
            val entity = entity {
                set("data")
                set(1)
            }

            // act
            entity.remove<Int>()

            // assert
            called shouldBe 0
        }

        @Test
        fun `should not call remove listener when added but not set component removed`() {
            // arrange
            var called = 0
            observe<OnRemove>().involving(query<String>()).exec {
                called++
            }
            val entity = entity {
                add<String>()
            }

            // act
            entity.remove<String>()

            // assert
            called shouldBe 0
        }

        @Test
        fun `should still remove component after remove listener modifies it`() {
            // arrange
            var called = 0
            observe<OnRemove>().involving(query<String>()).exec {
                called++
                entity.set("new data")
            }
            val entity = entity {
                add<String>()
            }

            // act
            entity.remove<String>()

            // assert
            assertSoftly {
                called shouldBe 0
                entity.get<String>() shouldBe null
            }
        }

        @Test
        fun `should correctly fire listener that listens to several removed components`() {
            // arrange
            var called = mutableListOf<Entity>()
            observe<OnRemove>().involving(query<String, Int>()).exec { (string, int) ->
                called.add(entity)
            }
            val entity1 = entity {
                set("data")
                set(1)
            }
            val entity2 = entity {
                set("data")
                set(1)
            }
            val entity3 = entity {
                set("data")
            }

            // act
            entity1.remove<Int>() // Fires
            entity2.remove<String>() // Fires
            entity3.remove<String>() // Doesn't fire

            // assert
            called shouldContainExactly listOf(entity1, entity2)
        }

        @Test
        fun `should correctly remove component if event listener modifies type`() {
            // arrange
            var called = 0
            observe<OnRemove>().involving(query<String>()).exec { (string) ->
                called++
                entity.set(1)
            }
            val entity = entity {
                set("data")
            }

            // act
            entity.remove<String>()

            // assert
            assertSoftly {
                called shouldBe 1
                entity.get<String>().shouldBeNull()
                entity.get<Int>() shouldBe 1
            }
        }
    }
}
