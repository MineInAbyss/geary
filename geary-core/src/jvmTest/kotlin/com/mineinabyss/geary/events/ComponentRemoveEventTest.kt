package com.mineinabyss.geary.events

import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.builders.listener
import com.mineinabyss.geary.systems.query.ListenerQuery
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
        geary.listener(object : ListenerQuery() {
            val string by get<String>()
            override fun ensure() = event.anyRemoved(::string)
        }).exec {
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
        geary.listener(object : ListenerQuery() {
            val string by get<String>()
            override fun ensure() = event.anyRemoved(::string)
        }).exec {
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
        geary.listener(object : ListenerQuery() {
            val string by get<String>()
            override fun ensure() = event.anyRemoved(::string)
        }).exec {
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
        geary.listener(object : ListenerQuery() {
            val string by get<String>()
            override fun ensure() = event.anyRemoved(::string)
        }).exec {
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
        geary.listener(object : ListenerQuery() {
            val string by get<String>()
            val int by get<Int>()
            override fun ensure() = event.anyRemoved(::string, ::int)
        }).exec { called.add(entity) }
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
}
