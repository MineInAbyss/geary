package com.mineinabyss.geary.datatypes

import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.datatypes.maps.Family2ObjectArrayMap
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import org.junit.jupiter.api.Test

class Family2ObjectArrayMapTest {
    @Test
    fun addAndRemoveOne() {
        val familyMap = Family2ObjectArrayMap<String>()
        familyMap.add("a", EntityType(ulongArrayOf(1uL, 2uL, 3uL)))
        familyMap.match(family { has(1uL) }) shouldContainExactly listOf("hello")
        familyMap.remove("a")
        familyMap.match(family { has(1uL) }).shouldBeEmpty()
    }


    @Test
    fun addAndRemoveReplacingWithOther() {
        val familyMap = Family2ObjectArrayMap<String>()
        familyMap.add("a", EntityType(ulongArrayOf(1uL, 2uL, 3uL)))
        familyMap.add("b", EntityType(ulongArrayOf(1uL, 3uL)))
        familyMap.match(family { has(1uL) }).shouldContainExactly("a", "b")
        familyMap.remove("a")
        familyMap.match(family {
            has(1uL)
        }).shouldContainExactly("b")
        familyMap.match(family {
            has(1uL)
            has(2uL)
        }).shouldContainExactly()
    }

    @Test
    fun removeLastInArray() {
        val familyMap = Family2ObjectArrayMap<String>()
        familyMap.add("a", EntityType(ulongArrayOf(1uL, 2uL, 3uL)))
        familyMap.add("b", EntityType(ulongArrayOf(1uL, 3uL)))
        familyMap.match(family { has(1uL) }).shouldContainExactly("a", "b")
        familyMap.remove("a")
        familyMap.remove("b")
        familyMap.match(family {
            has(1uL)
        }).shouldBeEmpty()
    }
}
