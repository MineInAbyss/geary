package com.mineinabyss.geary.datatypes

import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.datatypes.maps.Family2ObjectArrayMap
import com.mineinabyss.geary.engine.ComponentProvider
import com.mineinabyss.geary.engine.Components
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import org.junit.jupiter.api.Test
import kotlin.reflect.KClassifier

class MockComponentProvider : ComponentProvider{
    override val types: Components = Components(this)

    override fun getOrRegisterComponentIdForClass(kClass: KClassifier): ComponentId {
        return 0uL //TODO increment
    }
}

class Family2ObjectArrayMapTest {
    val comp = MockComponentProvider()

    @Test
    fun addAndRemoveOne() {
        val familyMap = Family2ObjectArrayMap<String>(comp.types)
        familyMap.add("a", EntityType(ulongArrayOf(1uL, 2uL, 3uL)))
        familyMap.match(family(comp) { has(1uL) }) shouldContainExactly listOf("a")
        familyMap.remove("a")
        familyMap.match(family(comp) { has(1uL) }).shouldBeEmpty()
    }


    @Test
    fun addAndRemoveReplacingWithOther() {
        val familyMap = Family2ObjectArrayMap<String>(comp.types)
        familyMap.add("a", EntityType(ulongArrayOf(1uL, 2uL, 3uL)))
        familyMap.add("b", EntityType(ulongArrayOf(1uL, 3uL)))
        familyMap.match(family(comp) { has(1uL) }).shouldContainExactly("a", "b")
        familyMap.remove("a")
        familyMap.match(family(comp) {
            has(1uL)
        }).shouldContainExactly("b")
        familyMap.match(family(comp) {
            has(1uL)
            has(2uL)
        }).shouldContainExactly()
    }

    @Test
    fun removeLastInArray() {
        val familyMap = Family2ObjectArrayMap<String>(comp.types)
        familyMap.add("a", EntityType(ulongArrayOf(1uL, 2uL, 3uL)))
        familyMap.add("b", EntityType(ulongArrayOf(1uL, 3uL)))
        familyMap.match(family(comp) { has(1uL) }).shouldContainExactly("a", "b")
        familyMap.remove("a")
        familyMap.remove("b")
        familyMap.match(family(comp) {
            has(1uL)
        }).shouldBeEmpty()
    }
}
