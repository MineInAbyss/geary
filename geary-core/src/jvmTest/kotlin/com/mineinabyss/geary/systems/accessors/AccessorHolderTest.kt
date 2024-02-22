package com.mineinabyss.geary.systems.accessors

import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.systems.query.Query
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Test

internal class AccessorHolderTest : GearyTest() {
    fun fancyQuery() = object : Query() {
        val default by target.get<String>().orDefault { "empty!" }
        val mapped by target.get<Int>().map { it.toString() }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun fancyAccessors() {
        val entity = entity()
        entity.set(1)
        //TODO reimplement test
//        FancyQuery.firstOrNull { it.entity == entity } shouldBe null
//        entity.set(1)
//        FancyQuery.run {
//            first { it.entity == entity }.apply {
//                default shouldBe "empty!"
//                mapped shouldBe "1"
//            }
//        }
    }
}
