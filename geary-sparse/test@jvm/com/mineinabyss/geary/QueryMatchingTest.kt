package com.mineinabyss.geary

import com.mineinabyss.geary.query.ComponentIndex
import com.mineinabyss.geary.query.Query
import org.junit.jupiter.api.Test

class QueryMatchingTest {
    @Test
    fun `should match query entities correctly`() {
        val components = ComponentIndex()
        val strings = components.get<String>(0L)
        val integers = components.get<Int>(1L)
        strings[100] = "hello"
        integers[100] = 100
        integers[101] = 101
        strings[8000] = "abc"
        integers[8000] = 101
        val query = Query(arrayOf(strings, integers))
        query.forEach {
            println(it)
        }
    }
}