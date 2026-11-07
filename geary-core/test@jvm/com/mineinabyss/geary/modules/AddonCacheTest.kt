package com.mineinabyss.geary.modules

import com.mineinabyss.dependencies.gets
import com.mineinabyss.dependencies.single
import com.mineinabyss.geary.addons.gearyAddon
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import org.junit.jupiter.api.Test

class AddonCacheTest {
    class Counter

    private val addon = gearyAddon("cache-test") { single { Counter() } }.gets<Counter>()

    @Test
    fun `returns the same value until the addon unloads`() {
        val world = geary(TestEngineModule)
        world.install(addon)
        val first = world.getAddon(addon)
        world.getAddon(addon) shouldBeSameInstanceAs first

        world.scope.unload(addon)
        world.addonOrNull(addon).shouldBeNull()

        world.install(addon)
        world.getAddon(addon) shouldNotBeSameInstanceAs first
        world.addonOrNull(addon) shouldBe world.getAddon(addon)
    }
}
