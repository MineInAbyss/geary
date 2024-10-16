package com.mineinabyss.geary.observers.entity_scoped

import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.test.GearyTest
import com.mineinabyss.geary.modules.observe
import com.mineinabyss.geary.observers.entity.observe
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
}
