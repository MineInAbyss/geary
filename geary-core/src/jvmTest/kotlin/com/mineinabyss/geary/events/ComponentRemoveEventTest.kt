package com.mineinabyss.geary.events

import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.events.types.OnRemove
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.builders.observe
import com.mineinabyss.geary.systems.query.Query
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ComponentRemoveEventTest : GearyTest() {
    @BeforeEach
    fun reset() {
        resetEngine()
    }

    @Test
    fun `should fire remove listener and have access to removed component data when component removed`() {
        // arrange
        var called = 0
        geary.observe<OnRemove>().involving<String>().exec { (string) ->
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

        geary.observe<OnRemove>().involving<String>().exec {
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
        geary.observe<OnRemove>().involving<String>().exec {
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
        geary.observe<OnRemove>().involving<String>().exec {
            called++
            entity.set("new data")
        }
        val entity = entity {
            add<String>()
        }

        // act
        entity.remove<String>()

        // assert
        called shouldBe 0
        entity.get<String>() shouldBe null
    }

    @Test
    fun `should correctly fire listener that listens to several removed components`() {
        // arrange
        var called = mutableListOf<Entity>()
        geary.observe<OnRemove>().involving<String, Int>().exec { (string, int) ->
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
        called.filter { it == entity1 }.size shouldBe 1
        called.filter { it == entity2 }.size shouldBe 1
        called.filter { it == entity3 }.size shouldBe 0
    }

    @Test
    fun `should correctly remove component if event listener modifies type`() {
        // arrange
        var called = 0
        geary.observe<OnRemove>().involving<String>().exec { (string) ->
            called++
            entity.set(1)
        }
        val entity = entity {
            set("data")
        }

        // act
        entity.remove<String>()

        // assert
        called shouldBe 1
        entity.get<String>().shouldBeNull()
        entity.get<Int>() shouldBe 1
    }
}
