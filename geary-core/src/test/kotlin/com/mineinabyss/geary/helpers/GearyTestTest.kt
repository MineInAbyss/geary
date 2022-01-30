package com.mineinabyss.geary.helpers

import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class GearyTestTest: GearyTest() {
    var staticEngine = engine

    @Test
    fun `clear engine`() = runTest {
        clearEngine()
        engine shouldNotBe staticEngine
    }
}
