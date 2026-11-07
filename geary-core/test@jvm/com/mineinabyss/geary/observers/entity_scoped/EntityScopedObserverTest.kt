package com.mineinabyss.geary.observers.entity_scoped

import com.mineinabyss.dependencies.get
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.engine.archetypes.ArchetypeQueryManager
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.test.GearyTest
import com.mineinabyss.geary.modules.observe
import com.mineinabyss.geary.observers.entity.observe
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class EntityScopedObserverTest : GearyTest() {
    private sealed class Clicked

    @Test
    fun `should only observe scoped events on correct entity`() {
        // arrange
        val scopedObserve = entity()
        val globallyObserve = entity()
        val lines = mutableListOf<String>()
        scopedObserve.observe<Clicked>().exec { lines += "Scoped clicked $entity" }
        observe<Clicked>().exec { lines += "Global clicked $entity" }

        // act
        scopedObserve.emit<Clicked>()
        globallyObserve.emit<Clicked>()

        // assert
        lines shouldBe """
            Scoped clicked $scopedObserve
            Global clicked $scopedObserve
            Global clicked $globallyObserve
        """.trimIndent().lines()
    }

    @Test
    fun `should propagate scoped observers to instances when instance created after tracking observer`() {
        // arrange
        val prefab = entity()
        val lines = mutableListOf<String>()
        prefab.observe<Clicked>().exec { lines += "Scoped clicked $entity" }
        val instance = entity { extend(prefab) }

        // act
        instance.emit<Clicked>()

        // assert
        lines shouldBe """
            Scoped clicked $instance
        """.trimIndent().lines()

    }

    @Test
    fun `should propagate scoped observers to instances when instance created before tracking observer`() {
        // arrange
        val prefab = entity()
        val instance = entity { extend(prefab) }
        val lines = mutableListOf<String>()
        prefab.observe<Clicked>().exec { lines += "Scoped clicked $entity" }

        // act
        instance.emit<Clicked>()

        // assert
        lines shouldBe """
            Scoped clicked $instance
        """.trimIndent().lines()
    }

    @Test
    fun `instance of a prefab with an observer has no children`() {
        val prefab = entity()
        var runs = 0
        prefab.observe<Clicked>().exec { runs++ }
        val instance = entity { extend(prefab) }

        instance.emit<Clicked>()

        instance.children.shouldBeEmpty()
        runs shouldBe 1
    }

    @Test
    fun `removing an instance keeps the prefab observer working`() {
        val prefab = entity()
        val lines = mutableListOf<String>()
        prefab.observe<Clicked>().exec { lines += "Scoped clicked $entity" }
        entity { extend(prefab) }.removeEntity()
        val second = entity { extend(prefab) }

        second.emit<Clicked>()

        lines shouldBe listOf("Scoped clicked $second")
    }

    @Test
    fun `instance churn creates no archetypes`() {
        val queryManager = get<ArchetypeQueryManager>()
        val prefab = entity()
        prefab.observe<Clicked>().exec { }
        entity { extend(prefab) }.removeEntity()
        val archetypesBefore = queryManager.archetypeCount

        val alive = ArrayDeque<Entity>()
        repeat(200) {
            alive.addLast(entity { extend(prefab) })
            if (alive.size > 20) alive.removeFirst().removeEntity()
        }
        alive.forEach { it.removeEntity() }

        queryManager.archetypeCount shouldBe archetypesBefore
    }
}
