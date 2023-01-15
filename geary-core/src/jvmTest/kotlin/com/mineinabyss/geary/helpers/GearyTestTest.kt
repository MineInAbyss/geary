package com.mineinabyss.geary.helpers

import com.mineinabyss.geary.helpers.tests.GearyTest
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

class GearyTestTest: GearyTest() {
    @Test
    fun `clear engine`() {
        val engine = geary.engine
        resetEngine()
        geary.engine shouldNotBe engine
    }
}
