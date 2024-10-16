package com.mineinabyss.geary.components

import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.test.GearyTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ComponentAsEntityProviderTest: GearyTest() {
    @Test
    fun `should correctly register reserved components`() {
        entity()
        componentId<Any>() shouldBe ReservedComponents.ANY
    }
}
