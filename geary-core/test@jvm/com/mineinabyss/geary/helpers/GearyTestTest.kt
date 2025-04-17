package com.mineinabyss.geary.helpers

import com.mineinabyss.geary.datatypes.maps.ArrayTypeMap
import com.mineinabyss.geary.test.GearyTest
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.koin.dsl.koinApplication
import org.koin.dsl.module

class GearyTestTest: GearyTest() {
    @Test
    fun `clear engine`() {
        val engine = this.engine
        resetEngine()
        this.engine shouldNotBe engine
    }

    @Test
    // This is testing a behaviour of koin that I didn't expect, keep this here in case it changes in the future
    fun `reusing koin modules should reuse exact class instances`() {
        val module = module {
            single { ArrayTypeMap() }
        }
        koinApplication { modules(module) }.koin.get<ArrayTypeMap>() shouldBe koinApplication { modules(module) }.koin.get<ArrayTypeMap>()
    }
}
