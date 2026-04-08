package com.mineinabyss.geary.helpers

import com.mineinabyss.geary.test.GearyTest
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

class GearyTestTest : GearyTest() {
    @Test
    fun `clear engine`() {
        val engine = world.engine
        resetEngine()
        world.engine shouldNotBe engine
    }
}
