package com.mineinabyss.geary.systems.accessors

import com.mineinabyss.geary.datatypes.GearyRecord
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.systems.query.GearyQuery
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Test

internal class AccessorHolderTest : GearyTest() {
    object FancyQuery : GearyQuery() {
        val GearyRecord.default by getOrDefault<String> { "empty!" }
        val GearyRecord.mapped by get<Int>().map { it.toString() }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun fancyAccessors() {
        val entity = entity()
        entity.set(1)
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
