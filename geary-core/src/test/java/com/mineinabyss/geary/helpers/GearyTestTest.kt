package com.mineinabyss.geary.helpers

import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

class GearyTestTest: GearyTest() {
    var staticEngine = engine

    @Test
    fun `clear engine`() {
        clearEngine()
        engine shouldNotBe staticEngine
    }
}
