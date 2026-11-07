package com.mineinabyss.geary.datatypes

import com.mineinabyss.dependencies.get
import com.mineinabyss.geary.datatypes.maps.ArrayTypeMap
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.test.GearyTest
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

class ArrayTypeMapTest : GearyTest() {
    private sealed class Marker

    @Test
    fun `new archetype caches its index on first set`() {
        val records = get<ArrayTypeMap>()
        val instance = entity()
        instance.add<Marker>()
        val archetype = records.runOn(instance.id) { archetype, _ -> archetype }

        archetype.indexInRecords shouldNotBe -1
        records.archList[archetype.indexInRecords] shouldBe archetype
    }

    @Test
    fun `entities in the same archetype share one record index`() {
        val records = get<ArrayTypeMap>()
        val first = entity().apply { add<Marker>() }
        val second = entity().apply { add<Marker>() }
        val sizeAfter = records.archList.size

        records.runOn(first.id) { archetype, _ -> archetype } shouldBe records.runOn(second.id) { archetype, _ -> archetype }
        entity().add<Marker>()
        records.archList.size shouldBe sizeAfter
    }
}
