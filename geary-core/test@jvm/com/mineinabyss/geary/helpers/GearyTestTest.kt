package com.mineinabyss.geary.helpers

import com.mineinabyss.geary.test.GearyTest
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

class GearyTestTest : GearyTest() {
    @Test
    fun `clear engine`() {
        val engine = this.engine
        resetEngine()
        this.engine shouldNotBe engine
    }
}
