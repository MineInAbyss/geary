package com.mineinabyss.geary.observers

import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.test.GearyTest
import com.mineinabyss.geary.modules.observe
import com.mineinabyss.geary.modules.observeWithData
import com.mineinabyss.geary.systems.query.query
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test

class ObserverTypeTests : GearyTest() {
    class MyEvent
    class OtherEvent

    @BeforeEach
    fun reset() = resetEngine()

    @Test
    fun `should observe event regardless of holding data when not observing event data`() {
        var called = 0
        observe<MyEvent>().exec { called++ }
        val entity = entity()

        called shouldBe 0
        entity.emit<MyEvent>()
        called shouldBe 1
        entity.emit(MyEvent())
        called shouldBe 2
        entity.emit<OtherEvent>()
        called shouldBe 2
    }

    @Test
    fun `should not observe event without data when observing data`() {
        var called = 0
        observeWithData<MyEvent>().exec {
            event.shouldBeInstanceOf<MyEvent>()
            called++
        }
        val entity = entity()

        called shouldBe 0
        entity.emit<MyEvent>()
        called shouldBe 0
        entity.emit(MyEvent())
        called shouldBe 1
        entity.emit(OtherEvent())
        called shouldBe 1
    }

    @Test
    fun `should observe events involving component when filtering one involved component`() {
        var called = 0
        observe<MyEvent>().involving(query<Int>()).exec { (int) -> called += int }

        val entity = entity()

        called shouldBe 0
        entity.emit<MyEvent>()
        called shouldBe 0
        entity.emit<MyEvent>(involving = componentId<Int>())
        called shouldBe 0
        entity.set<Int>(10)
        entity.emit<MyEvent>(involving = componentId<Int>())
        called shouldBe 10
    }
}
