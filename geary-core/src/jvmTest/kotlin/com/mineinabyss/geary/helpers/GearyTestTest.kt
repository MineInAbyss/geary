package com.mineinabyss.geary.helpers

import com.mineinabyss.geary.helpers.tests.GearyTest
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

class GearyTestTest: GearyTest() {
    var staticEngine = geary.engine

    @Test
    fun `clear engine`() {
        clearEngine()
        geary.engine shouldNotBe staticEngine
    }
}
